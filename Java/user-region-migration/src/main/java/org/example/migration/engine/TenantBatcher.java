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
 * 租户级并发处理器（ADR-0003）。
 *
 * <p>每个租户一个 {@link CompletableFuture}，提交到固定大小（{@code threads}）线程池，
 * pool 自然并发 {@code threads} 个租户。{@code batchSize} 仅作进度汇报粒度，不再驱动并发。
 *
 * <p>这是 {@link MigrationEngine} 的内部模块（package-private）。
 *
 * <p>单租户超时：每个租户的 processor 可配置最大执行时间，超时抛异常由上层隔离。
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
     * @param batchSize            进度汇报粒度（每完成一批打一次日志）
     * @param threads              租户级并发线程数
     * @param tenantTimeoutMinutes 单租户迁移超时（分钟），≤0 表示不设超时
     */
    TenantBatcher(int batchSize, int threads, long tenantTimeoutMinutes) {
        this.batchSize = Math.max(1, batchSize);
        this.threads = Math.max(1, threads);
        this.tenantTimeoutMinutes = tenantTimeoutMinutes;
    }

    /**
     * 租户级并发处理。每租户一个任务提交到线程池。
     *
     * @param tenantIds 全量租户列表
     * @param processor 单租户处理器（Consumer）
     */
    void processConcurrently(List<String> tenantIds, Consumer<String> processor) {
        if (tenantIds.isEmpty()) {
            return;
        }

        // 进度汇报：按 batchSize 切片，记录每批的起止（不影响并发）
        List<List<String>> progressBatches = partition(tenantIds, batchSize);
        log.info("tenant batcher: {} tenants, {} progress-batches, {} threads",
                tenantIds.size(), progressBatches.size(), threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String tenantId : tenantIds) {
            futures.add(CompletableFuture.runAsync(
                    () -> processTenantWithTimeout(tenantId, processor), executor));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            throw new RuntimeException("migration processing interrupted", e);
        } finally {
            shutdownQuietly(executor);
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

    private void shutdownQuietly(ExecutorService executor) {
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

    /** 切片（仅用于进度汇报日志） */
    static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batches.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return batches;
    }
}
