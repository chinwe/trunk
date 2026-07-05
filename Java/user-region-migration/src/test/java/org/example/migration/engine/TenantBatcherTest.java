package org.example.migration.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TenantBatcher 测试：验证批切分与批间并发行为（ADR-0004）。
 */
class TenantBatcherTest {

    @Test
    @DisplayName("partition 按 batchSize 切分列表")
    void shouldPartitionBySize() {
        List<String> items = List.of("a", "b", "c", "d", "e");
        List<List<String>> batches = TenantBatcher.partition(items, 2);
        assertThat(batches).hasSize(3);
        assertThat(batches.get(0)).containsExactly("a", "b");
        assertThat(batches.get(1)).containsExactly("c", "d");
        assertThat(batches.get(2)).containsExactly("e");
    }

    @Test
    @DisplayName("processBatchesConcurrently 处理所有租户,每批接收一批而非单个")
    void shouldProcessAllTenantsInBatches() {
        TenantBatcher batcher = new TenantBatcher(2, 1); // 每批2个,1线程
        List<String> processed = Collections.synchronizedList(new ArrayList<>());
        batcher.processBatchesConcurrently(List.of("t1", "t2", "t3", "t4", "t5"),
                batch -> processed.addAll(batch));
        assertThat(processed).containsExactlyInAnyOrder("t1", "t2", "t3", "t4", "t5");
    }

    @Test
    @DisplayName("并发多线程时所有批仍被完整处理")
    void shouldHandleMultipleThreadsWithBatches() {
        TenantBatcher batcher = new TenantBatcher(3, 4); // 批次大小3,4线程
        List<String> tenants = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            tenants.add("t" + i);
        }
        List<String> processed = Collections.synchronizedList(new ArrayList<>());
        batcher.processBatchesConcurrently(tenants, batch -> processed.addAll(batch));
        assertThat(processed).hasSize(20);
        assertThat(processed).containsAll(tenants);
    }

    @Test
    @DisplayName("空租户列表不抛异常")
    void shouldHandleEmptyList() {
        TenantBatcher batcher = new TenantBatcher(50, 1);
        batcher.processBatchesConcurrently(List.of(), batch -> { });
        // 不抛异常即通过
    }

    @Test
    @DisplayName("单租户+单批次边界")
    void shouldHandleSingleTenant() {
        TenantBatcher batcher = new TenantBatcher(50, 1);
        AtomicInteger batchCount = new AtomicInteger();
        batcher.processBatchesConcurrently(List.of("t1"), batch -> batchCount.incrementAndGet());
        assertThat(batchCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("批切分粒度正确:每批最多 batchSize 个")
    void shouldEnforceBatchSize() {
        TenantBatcher batcher = new TenantBatcher(2, 1);
        List<List<String>> receivedBatches = Collections.synchronizedList(new ArrayList<>());
        batcher.processBatchesConcurrently(List.of("t1", "t2", "t3", "t4", "t5"),
                receivedBatches::add);
        // 5 个租户, batch=2 → 3 批 [t1,t2] [t3,t4] [t5]
        assertThat(receivedBatches).hasSize(3);
        assertThat(receivedBatches.get(0)).hasSize(2);
        assertThat(receivedBatches.get(1)).hasSize(2);
        assertThat(receivedBatches.get(2)).hasSize(1);
    }
}
