package org.example.migration.engine;

import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.MigrationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试用记录型切流动作：实现公共 SPI，记录是否被调用及收到哪些租户。
 * 通过 evictCalled 标志验证"闸门通过才切流"。
 */
public class RecordingCutoverAction implements CutoverAction {

    private boolean evictCalled = false;
    private final List<String> evictedTenants = new ArrayList<>();

    @Override
    public void evict(MigrationContext ctx, List<String> tenantIds, String product, String bizLine) {
        evictCalled = true;
        evictedTenants.addAll(tenantIds);
    }

    public boolean isEvictCalled() {
        return evictCalled;
    }

    public List<String> getEvictedTenants() {
        return evictedTenants;
    }
}
