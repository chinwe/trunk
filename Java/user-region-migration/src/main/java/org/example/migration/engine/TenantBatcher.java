package org.example.migration.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * 租户分批处理器——管理分批切片、线程池与批次间并发。
 *
 * 这是 {@link MigrationEngine} 的内部模块（package-private），
 * 逻辑简单且与引擎的租户处理循环强耦合，不暴露为 SPI。
 *
 * 单批内串行：同一批次内逐租户调用 processor。
 * 批次间并发：多批次在线程池中并行执行。
 * 单租户超时：每个租户的 processor 可配置最大执行时间，超时抛异常由上层隔离。
 */
class TenantBatcher {

    private static final Logger log = LoggerFactory.getLogger(TenantBatcher.class);

    private final int batchSize;
    private final int threads;
    private final long tenantTimeoutMinutes;

    TenantBatcher(int batchSize, int threads) {
        this(batchSize, threads, 0);
    }

    /**
     * @param batchSize            每批租户数
     * @param threads              批次间并发线程数
     * @param tenantTimeoutMinutes 单租户迁移超时（分钟），≤0 表示不设超时
     */
    TenantBatcher(int batchSize, int threads, long tenantTimeoutMinutes) {
        this.batchSize = Math.max(1, batchSize);
        this.threads = Math.max(1, threads);
        this.tenantTimeoutMinutes = tenantTimeoutMinutes;
    }

    /**
     * 分批并发处理租户列表。单批内串行调用 processor。
     *
     * @param tenantIds 全量租户列表
     * @param processor 单租户处理器（Consumer）
     */
    void processConcurrently(List<String> tenantIds, Consumer<String> processor) {
        List<List<String>> batches = partition(tenantIds, batchSize);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<String> batch : batches) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (String tenantId : batch) {
                    processTenantWithTimeout(tenantId, processor);
                }
            }, executor));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            throw new RuntimeException("migration batch processing interrupted", e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /** 单租户处理（含超时控制） */
    private void processTenantWithTimeout(String tenantId, Consumer<String> processor) {
        if (tenantTimeoutMinutes <= 0) {
            processor.accept(tenantId);
            return;
        }
        try {
            CompletableFuture.runAsync(() -> processor.accept(tenantId))
                    .orTimeout(tenantTimeoutMinutes, TimeUnit.MINUTES)
                    .get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                throw new RuntimeException(
                        "tenant " + tenantId + " migration timed out after " + tenantTimeoutMinutes + " min", cause);
            }
            throw new RuntimeException(cause != null ? cause : e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted during tenant migration", e);
        }
    }

    static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batches.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return batches;
    }
}
