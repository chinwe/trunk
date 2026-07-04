package org.example.migration.domain;

/**
 * 迁移方向。
 * FORWARD = 正向迁移（如新加坡→缅甸）；ROLLBACK = 逆向回滚（如缅甸→新加坡）。
 * 两者复用同一个 task.migrate，仅 source/target 对调。
 */
public enum Direction {
    FORWARD,
    ROLLBACK
}
