package org.example.migration.engine;

import org.example.migration.domain.RegionName;

/**
 * 迁移通知器。在切流完成后发送迁出/迁入 Kafka 通知，业务据此清理内存数据、切换路由。
 *
 * <p>两阶段（ADR-0005）：单次 Run 会发两次通知——CORE 切流后一次（payload 含
 * {@code phase=CORE_CUTOVER}），SECONDARY 完成后一次（payload 含 {@code phase=ALL_DONE}）。
 *
 * <p><b>投递语义</b>：至少一次（at-least-once）。崩溃恢复时框架可能重发同一阶段的通知，
 * 消费者必须按 {@code runId + phase} 自处理乱序与重复——框架不做跨分区顺序保证。
 *
 * 抽象为接口便于测试注入 fake。生产实现用 Kafka 发送通知。
 */
public interface MigrationNotifier {

    /**
     * 发送迁移通知。
     *
     * @param sourceRegion 迁出区域
     * @param targetRegion 迁入区域
     * @param payload      通知内容（含 runId / tenants / phase 等 k=v 字段）
     */
    void notify(RegionName sourceRegion, RegionName targetRegion, String payload);

    /** 空实现：未配置通知器时的兜底 */
    MigrationNotifier NO_OP = (s, t, p) -> { };
}
