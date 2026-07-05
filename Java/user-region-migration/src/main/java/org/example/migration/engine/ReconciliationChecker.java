package org.example.migration.engine;

import org.example.migration.domain.MigrationPhase;
import org.example.migration.domain.entity.MigrationRun;

import java.util.List;

/**
 * 对账校验器 SPI（业务自证一致性，ADR-0001；两阶段扩展 ADR-0005）。
 *
 * <p>切流前由 {@link CheckerReconciliationGate} 调用。业务用任意算法证明源/目标数据一致：
 * count 差、checksum、抽样、逐条比对或组合。框架只接受二值判定，不预设算法。
 *
 * <p>为什么不是"count 源 vs 目标相等"？因为业务 migrate 是真搬迁（删源）语义，
 * 被搬的数据在源区已删，源 count ≈ 0、目标 count = N，"相等"在数学上不成立。
 * 业务自证一致性让算法贴合具体数据模型，避免框架硬编码错误算法。
 *
 * <p>两阶段（ADR-0005）：业务按 {@link MigrationPhase} 分别对账核心数据集（CORE 阶段，
 * 是切流前置闸门）与次核心数据集（SECONDARY 阶段，是收尾闸门，不通过则 Run 标 FAILED
 * 等人工，不自动 rollback）。
 *
 * @see CheckerReconciliationGate
 */
public interface ReconciliationChecker {

    /**
     * 证明本次迁移范围内的数据在源/目标 region 间是否一致。
     *
     * @param run               当前迁移执行（含 source/target region、product、bizLine、当前 phase）
     * @param migratedTenantIds 本次刚迁移完成的租户ID列表（业务可据此限定校验范围）
     * @param phase             迁移阶段（CORE / SECONDARY），业务据此分流对账数据集
     * @return true=一致；false=不一致，需人工介入
     */
    boolean consistent(MigrationRun run, List<String> migratedTenantIds, MigrationPhase phase);
}
