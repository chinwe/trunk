package org.example.migration.engine;

import org.example.migration.domain.entity.MigrationRun;

/**
 * 总量对账闸门。在 migrate 完成数据搬运、切流之前由引擎调用。
 *
 * 职责：对各中间件做 COUNT 级轻量校验（源 vs 目标），返回是否一致。
 * 通过才允许切流；不通过则停下等待人工介入。
 *
 * 抽象为接口的目的：引擎测试可注入 fake 闸门（返回可控结果），
 * 不依赖真实中间件。生产实现通过查询各中间件客户端统计总数。
 */
public interface ReconciliationGate {

    /**
     * 执行总量对账。
     *
     * @param run 当前迁移执行（含 source/target region、product、bizLine）
     * @return true=通过可切流，false=不一致需人工介入
     */
    boolean check(MigrationRun run);
}
