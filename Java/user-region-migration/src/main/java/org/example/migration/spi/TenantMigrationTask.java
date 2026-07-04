package org.example.migration.spi;

import org.example.migration.spi.result.MigrationResult;
import org.example.migration.spi.result.VerifyResult;

import java.util.List;

/**
 * 核心业务插件 SPI。业务组件实现此接口，由框架按租户分批后驱动。
 *
 * 设计原则：方向无关。
 *   业务只实现 migrate 一个方法，正向迁移与逆向回滚复用同一份逻辑。
 *   正向：框架注入 source=源区, target=目标区
 *   回滚：框架注入 source=原目标区, target=原源区（对调）
 *   业务通过 ctx.sourceRegion()/targetRegion() 获取客户端，禁止硬编码 region。
 */
public interface TenantMigrationTask {

    /** 任务标识，如 "user-migration"。命令通过 --task 指定。 */
    String taskName();

    /**
     * 唯一必须实现的方法 —— 方向无关的数据迁移。
     *
     * <p><b>幂等契约（必须遵守，ADR-0002）</b>：本方法必须幂等——同一租户被调用 N 次
     * 与 1 次的结果等价。框架的 resume / retry / rollback 都可能对同一租户重复调用 migrate，
     * 非幂等实现会在第一次重试时产生重复数据。建议用 {@code INSERT ... ON DUPLICATE KEY UPDATE} /
     * {@code INSERT IGNORE} / 先删后插 / UPSERT 等模式；S3 等按键寻址的中间件天然幂等。
     *
     * <p>框架按租户分批（默认 50/批，可配）后调用此方法，业务自行查询并迁移数据。
     * 单租户内跨中间件的补偿回滚由业务在此方法内自管（框架无法代劳）。
     *
     * @param ctx       迁移上下文（含源/目标 region 客户端）
     * @param tenantIds 本批租户ID列表
     * @param product   产品标识（命令行透传）
     * @param bizLine   业务线标识（命令行透传）
     * @return 迁移结果
     */
    MigrationResult migrate(MigrationContext ctx, List<String> tenantIds, String product, String bizLine);

    /**
     * 可选：深度对账。默认未实现，业务可覆盖做总量/抽样/逐条校验。
     * 框架的 verify 命令会调用此方法。
     */
    default VerifyResult verify(MigrationContext ctx, List<String> tenantIds, String product, String bizLine) {
        return VerifyResult.unimplemented();
    }
}
