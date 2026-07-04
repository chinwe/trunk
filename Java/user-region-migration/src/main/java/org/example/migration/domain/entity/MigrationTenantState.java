package org.example.migration.domain.entity;

import lombok.Data;
import org.example.migration.domain.TenantStatus;

import java.time.LocalDateTime;

/**
 * 单个租户在某次迁移执行中的状态。对应 migration_tenant_state 表。
 */
@Data
public class MigrationTenantState {

    private String runId;
    private String tenantId;
    private TenantStatus status;
    private String errorContext;
    private LocalDateTime updatedAt;
}
