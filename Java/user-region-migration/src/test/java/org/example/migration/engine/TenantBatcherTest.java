package org.example.migration.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TenantBatcher 测试：验证分批切片与并发处理行为。
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
    @DisplayName("processConcurrently 处理所有租户,单线程内顺序调用")
    void shouldProcessAllTenants() {
        TenantBatcher batcher = new TenantBatcher(2, 1); // 每批2个,1线程
        List<String> processed = Collections.synchronizedList(new ArrayList<>());
        batcher.processConcurrently(List.of("t1", "t2", "t3", "t4", "t5"), processed::add);
        assertThat(processed).containsExactlyInAnyOrder("t1", "t2", "t3", "t4", "t5");
    }

    @Test
    @DisplayName("并发多线程时所有租户仍被完整处理")
    void shouldHandleMultipleThreads() {
        TenantBatcher batcher = new TenantBatcher(3, 4); // 批次大小3,4线程
        List<String> tenants = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            tenants.add("t" + i);
        }
        List<String> processed = Collections.synchronizedList(new ArrayList<>());
        batcher.processConcurrently(tenants, processed::add);
        assertThat(processed).hasSize(20);
        assertThat(processed).containsAll(tenants);
    }

    @Test
    @DisplayName("空租户列表不抛异常")
    void shouldHandleEmptyList() {
        TenantBatcher batcher = new TenantBatcher(50, 1);
        batcher.processConcurrently(List.of(), t -> {});
        // 不抛异常即通过
    }

    @Test
    @DisplayName("单租户+单批次边界")
    void shouldHandleSingleTenant() {
        TenantBatcher batcher = new TenantBatcher(50, 1);
        AtomicInteger count = new AtomicInteger();
        batcher.processConcurrently(List.of("t1"), t -> count.incrementAndGet());
        assertThat(count.get()).isEqualTo(1);
    }
}
