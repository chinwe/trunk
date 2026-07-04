package org.example.migration.domain;

/**
 * 一次迁移执行（migration_run）的状态。
 *
 * 状态流转：RUNNING → DONE（闸门通过）或 FAILED（闸门不通过或有租户失败）。
 */
public enum RunStatus {
    RUNNING,
    DONE,
    FAILED
}
