package org.example.migration.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 租户分批处理器——管理分批切片、线程池与批次间并发。
 *
 * 这是 {@link MigrationEngine} 的内部模块（package-private），
 * 逻辑简单且与引擎的租户处理循环强耦合，不暴露为 SPI。
 *
 * 单批内串行：同一批次内逐租户调用 processor。
 * 批次间并发：多批次在线程池中并行执行。
 */
class TenantBatcher {

    private final int batchSize;
    private final int threads;

    TenantBatcher(int batchSize, int threads) {
        this.batchSize = Math.max(1, batchSize);
        this.threads = Math.max(1, threads);
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
                    processor.accept(tenantId);
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

    static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batches.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return batches;
    }
}
