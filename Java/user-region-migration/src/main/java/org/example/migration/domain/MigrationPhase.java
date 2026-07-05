package org.example.migration.domain;

/**
 * 迁移阶段（ADR-0005）。
 *
 * 两阶段迁移的标识，贯穿 {@code migrate} 与 {@code consistent} 两个 SPI 参数：
 * CORE = 用户登录后立即必需的数据（如基本信息、订单、账户余额），核心阶段完成后即切流；
 * SECONDARY = 短时缺失可容忍/可降级的数据（如历史行为、收藏、通知记录），切流后在窗口期内继续搬运。
 *
 * 框架硬编码"全 CORE 完成后切流、再全 SECONDARY"的编排顺序，不暴露 phase 序列配置。
 */
public enum MigrationPhase {
    CORE,
    SECONDARY
}
