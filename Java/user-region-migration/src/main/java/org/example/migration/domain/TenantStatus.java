package org.example.migration.domain;

/**
 * 单个租户在某次迁移执行中的状态（migration_tenant_state）。
 */
public enum TenantStatus {
    PENDING,
    RUNNING,
    DONE,
    FAILED
}
