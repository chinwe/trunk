package org.example.migration.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 批间并发处理器（ADR-0004）。
 *
 * <p>按 {@code batchSize} 切批，每批一个 {@link CompletableFuture} 提交到固定大小（{@code threads}）线程池。
 * 单批内串行（业务一批一个 migrate 调用）；批间并发（threads 个批并行）。
 *
 * <p>这是 {@link MigrationEngine} 的内部模块（package-private）。
 * 单批超时：每批可配置最大执行时间，超时抛异常由上层隔离。
 */
class TenantBatcher {

    private static final Logger log = LoggerFactory.getLogger(TenantBatcher.class);

    private final int batchSize;
    private final int threads;
    private final long batchTimeoutMinutes;

    TenantBatcher(int batchSize, int threads) {
        this(batchSize, threads, 0);
    }

    /**
     * @param batchSize           每批租户数（业务调用粒度）
     * @param threads             批间并发线程数
     * @param batchTimeoutMinutes 单批迁移超时（分钟），≤0 表示不设超时
     */
    TenantBatcher(int batchSize, int threads, long batchTimeoutMinutes) {
        this.batchSize = Math.max(1, batchSize);
        this.threads = Math.max(1, threads);
        this.batchTimeoutMinutes = batchTimeoutMinutes;
    }

    /**
     * 批间并发处理。每批一个任务提交到线程池，单批内串行调用 processor。
     * 超时直接挂在外层 CompletableFuture 上，不嵌套 runAsync——消除额外的 ForkJoinPool 线程。
     *
     * <p>processor 接收一批租户（而非单个租户），对应业务一次 {@code task.migrate(ctx, batchTenantIds, ...)} 调用。
     *
     * @param tenantIds 全量租户列表
     * @param processor 单批处理器（接收一批租户ID）
     */
    void processBatchesConcurrently(List<String> tenantIds, Consumer<List<String>> processor) {
        if (tenantIds.isEmpty()) {
            return;
        }
        List<List<String>> batches = partition(tenantIds, batchSize);
        log.info("tenant batcher: {} tenants, {} batches, {} threads",
                tenantIds.size(), batches.size(), threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<String> batch : batches) {
            CompletableFuture<Void> f = CompletableFuture.runAsync(
                    () -> processor.accept(batch), executor);
            if (batchTimeoutMinutes > 0) {
                f = f.orTimeout(batchTimeoutMinutes, TimeUnit.MINUTES);
            }
            futures.add(f);
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            throw new RuntimeException("migration batch processing interrupted", e);
        } finally {
            shutdownQuietly(executor);
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

    /** 切批 */
    static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batches.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return batches;
    }
}
