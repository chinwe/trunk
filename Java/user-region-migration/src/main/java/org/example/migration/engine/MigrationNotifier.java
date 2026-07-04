package org.example.migration.engine;

import org.example.migration.domain.RegionName;

/**
 * 迁移通知器。在切流完成后发送迁出/迁入 Kafka 通知，业务据此清理内存数据、切换路由。
 *
 * 抽象为接口便于测试注入 fake。生产实现用 Kafka 发送通知。
 */
public interface MigrationNotifier {

    /**
     * 发送迁移通知。
     *
     * @param sourceRegion 迁出区域
     * @param targetRegion 迁入区域
     * @param payload      通知内容
     */
    void notify(RegionName sourceRegion, RegionName targetRegion, String payload);

    /** 空实现：未配置通知器时的兜底 */
    MigrationNotifier NO_OP = (s, t, p) -> { };
}
