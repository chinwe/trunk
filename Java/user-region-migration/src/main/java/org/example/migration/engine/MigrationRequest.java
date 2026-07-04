package org.example.migration.engine;

import org.example.migration.domain.RegionName;

import java.util.List;

/**
 * 迁移执行请求。由 migrate 命令构造，传给 MigrationEngine。
 */
public class MigrationRequest {

    private final String taskName;
    private final RegionName sourceRegion;
    private final RegionName targetRegion;
    private final String product;
    private final String bizLine;
    private final List<String> tenantIds;
    private final int batchSize;
    private final int threads;

    public MigrationRequest(String taskName, RegionName sourceRegion, RegionName targetRegion,
                            String product, String bizLine, List<String> tenantIds,
                            int batchSize, int threads) {
        this.taskName = taskName;
        this.sourceRegion = sourceRegion;
        this.targetRegion = targetRegion;
        this.product = product;
        this.bizLine = bizLine;
        this.tenantIds = tenantIds;
        this.batchSize = batchSize;
        this.threads = threads;
    }

    public String getTaskName() { return taskName; }
    public RegionName getSourceRegion() { return sourceRegion; }
    public RegionName getTargetRegion() { return targetRegion; }
    public String getProduct() { return product; }
    public String getBizLine() { return bizLine; }
    public List<String> getTenantIds() { return tenantIds; }
    public int getBatchSize() { return batchSize; }
    public int getThreads() { return threads; }
}
