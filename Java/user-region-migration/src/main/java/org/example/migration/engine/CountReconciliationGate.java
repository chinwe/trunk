package org.example.migration.engine;

import org.example.migration.domain.RegionName;
import org.example.migration.domain.entity.MigrationRun;
import org.slf4j.Logger;

import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * 总量对账闸门：在 migrate 完成、切流前校验源/目标 region 的记录数是否一致。
 *
 * 计数方式由业务通过 {@link ReconciliationCounter} SPI 提供（框架无法预知业务表/索引结构）。
 * 若未提供 Counter，则默认通过（等价于 AlwaysPassReconciliationGate，保持向后兼容）。
 */
public class CountReconciliationGate implements ReconciliationGate {

    private static final Logger log = LoggerFactory.getLogger(CountReconciliationGate.class);

    private final ReconciliationCounter counter;

    public CountReconciliationGate(ReconciliationCounter counter) {
        this.counter = counter;
    }

    @Override
    public boolean check(MigrationRun run, List<String> migratedTenantIds) {
        if (counter == null) {
            log.info("reconciliation gate: no counter configured, pass by default");
            return true;
        }
        long sourceCount = counter.count(run.getSourceRegion(), run);
        long targetCount = counter.count(run.getTargetRegion(), run);
        boolean pass = sourceCount == targetCount;
        log.info("reconciliation gate: source={} count={}, target={} count={}, migratedTenants={}, pass={}",
                run.getSourceRegion(), sourceCount, run.getTargetRegion(), targetCount,
                migratedTenantIds != null ? migratedTenantIds.size() : 0, pass);
        return pass;
    }
}
