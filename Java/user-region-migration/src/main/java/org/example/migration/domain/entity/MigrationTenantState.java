package org.example.migration.domain.entity;

import org.example.migration.domain.TenantStatus;

import java.time.LocalDateTime;

/**
 * 单个租户在某次迁移执行中的状态。对应 migration_tenant_state 表。
 */
public class MigrationTenantState {

    private String runId;
    private String tenantId;
    private TenantStatus status;
    private String errorContext;
    private LocalDateTime updatedAt;

    public MigrationTenantState() {
    }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public TenantStatus getStatus() { return status; }
    public void setStatus(TenantStatus status) { this.status = status; }
    public String getErrorContext() { return errorContext; }
    public void setErrorContext(String errorContext) { this.errorContext = errorContext; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
