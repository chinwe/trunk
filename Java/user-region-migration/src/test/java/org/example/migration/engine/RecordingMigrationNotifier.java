package org.example.migration.engine;

import org.example.migration.domain.RegionName;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试用记录型通知器：记录每次 notify 的 payload，验证两阶段双通知（ADR-0005 Q7）。
 */
public class RecordingMigrationNotifier implements MigrationNotifier {

    private final List<String> payloads = new ArrayList<>();

    @Override
    public void notify(RegionName sourceRegion, RegionName targetRegion, String payload) {
        payloads.add(payload);
    }

    public List<String> getPayloads() {
        return payloads;
    }
}
