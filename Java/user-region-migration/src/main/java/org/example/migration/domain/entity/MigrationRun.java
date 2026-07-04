package org.example.migration.domain.entity;

import org.example.migration.domain.Direction;
import org.example.migration.domain.RegionName;
import org.example.migration.domain.RunStatus;

import java.time.LocalDateTime;

/**
 * 一次迁移执行记录。对应 migration_run 表。
 */
public class MigrationRun {

    private String runId;
    private String taskName;
    private Direction direction;
    private RegionName sourceRegion;
    private RegionName targetRegion;
    private String product;
    private String bizLine;
    private RunStatus status;
    private int totalTenants;
    private int processedTenants;
    private int failedTenants;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    private String errorContext;
    /** 回滚 run 指向原正向 run，正向 run 为 null */
    private String parentRunId;

    public MigrationRun() {
    }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public RegionName getSourceRegion() { return sourceRegion; }
    public void setSourceRegion(RegionName sourceRegion) { this.sourceRegion = sourceRegion; }
    public RegionName getTargetRegion() { return targetRegion; }
    public void setTargetRegion(RegionName targetRegion) { this.targetRegion = targetRegion; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public String getBizLine() { return bizLine; }
    public void setBizLine(String bizLine) { this.bizLine = bizLine; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }
    public int getTotalTenants() { return totalTenants; }
    public void setTotalTenants(int totalTenants) { this.totalTenants = totalTenants; }
    public int getProcessedTenants() { return processedTenants; }
    public void setProcessedTenants(int processedTenants) { this.processedTenants = processedTenants; }
    public int getFailedTenants() { return failedTenants; }
    public void setFailedTenants(int failedTenants) { this.failedTenants = failedTenants; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getErrorContext() { return errorContext; }
    public void setErrorContext(String errorContext) { this.errorContext = errorContext; }
    public String getParentRunId() { return parentRunId; }
    public void setParentRunId(String parentRunId) { this.parentRunId = parentRunId; }
}
