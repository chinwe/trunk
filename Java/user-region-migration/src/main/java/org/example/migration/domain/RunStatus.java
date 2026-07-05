package org.example.migration.domain;

/**
 * 一次迁移执行（migration_run）的状态（ADR-0005 两阶段扩展）。
 *
 * 正向迁移状态流转：
 * <pre>
 * RUNNING_CORE → CORE_CUTOVER_DONE → RUNNING_SECONDARY → DONE
 *     ↓                ↓                       ↓
 *   FAILED          FAILED                  FAILED（已切流，禁 rollback，等人工向前修复）
 * </pre>
 *
 * 任何阶段对账失败 / 零失败检查不过 → FAILED。
 * ROLLBACK run 不分阶段：走 RUNNING_CORE → DONE（DONE 前不切流、不发通知）。
 */
public enum RunStatus {
    /** 正在搬核心数据（取代单阶段模型的 RUNNING）。 */
    RUNNING_CORE,
    /** 中间态：核心已对账 + 已踢登录 + 已发通知①；待进 SECONDARY。对外可见。 */
    CORE_CUTOVER_DONE,
    /** 正在搬次核心数据。 */
    RUNNING_SECONDARY,
    /** 全部完成（核心 + 次核心都对账通过）。 */
    DONE,
    /** 失败，等待人工处理或 resume。 */
    FAILED
}
