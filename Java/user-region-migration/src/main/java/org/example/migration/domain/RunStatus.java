package org.example.migration.domain;

/**
 * 一次迁移执行（migration_run）的状态。
 */
public enum RunStatus {
    INIT,
    RUNNING,
    PAUSED,
    DONE,
    FAILED
}
