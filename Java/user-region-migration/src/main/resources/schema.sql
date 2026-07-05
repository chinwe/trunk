-- 迁移状态表 DDL（MySQL）。
-- 供 JdbcCheckpointStore 使用。InMemoryCheckpointStore 不依赖此脚本。
--
-- 注意：本脚本用 CREATE TABLE IF NOT EXISTS，对已存在的表不会自动加列。
-- ADR-0005 引入 phase 列后，已有数据的 DB 需手工执行：
--   ALTER TABLE migration_run ADD COLUMN phase VARCHAR(16) AFTER status;

-- 一次迁移执行
CREATE TABLE IF NOT EXISTS migration_run (
    run_id            VARCHAR(64)  PRIMARY KEY,           -- 如 user-migration-run-001
    task_name         VARCHAR(128) NOT NULL,              -- 如 user-migration
    direction         VARCHAR(16)  NOT NULL,              -- FORWARD / ROLLBACK
    source_region     VARCHAR(32)  NOT NULL,              -- singapore / myanmar
    target_region     VARCHAR(32)  NOT NULL,
    product           VARCHAR(64),
    biz_line          VARCHAR(64),
    status            VARCHAR(24)  NOT NULL,              -- RUNNING_CORE/CORE_CUTOVER_DONE/RUNNING_SECONDARY/DONE/FAILED（ADR-0005）
    phase             VARCHAR(16),                        -- CORE/SECONDARY；ROLLBACK run 为 NULL
    total_tenants     INT,
    processed_tenants INT DEFAULT 0,
    failed_tenants    INT DEFAULT 0,
    started_at        DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    error_context     TEXT,
    parent_run_id     VARCHAR(64),                        -- 回滚 run 指向原正向 run
    INDEX idx_status (status),
    INDEX idx_task (task_name)
);

-- 租户级断点（每个租户一行）
CREATE TABLE IF NOT EXISTS migration_tenant_state (
    run_id          VARCHAR(64)  NOT NULL,
    tenant_id       VARCHAR(64)  NOT NULL,
    status          VARCHAR(16)  NOT NULL,                -- PENDING/RUNNING/DONE/FAILED
    error_context   TEXT,
    updated_at      DATETIME     NOT NULL,
    PRIMARY KEY (run_id, tenant_id),
    INDEX idx_run_status (run_id, status)
);
