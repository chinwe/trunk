package org.example.migration.engine;

import org.example.migration.domain.entity.MigrationRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 对账闸门：委托业务提供的 {@link ReconciliationChecker} 自证一致性（ADR-0001）。
 *
 * <p>校验通过才允许切流；不通过则停下等待人工介入。
 * 若未提供 Checker Bean，引擎装配时改用 {@link AlwaysPassReconciliationGate} 默认通过。
 *
 * @see ReconciliationChecker
 */
public class CheckerReconciliationGate implements ReconciliationGate {

    private static final Logger log = LoggerFactory.getLogger(CheckerReconciliationGate.class);

    private final ReconciliationChecker checker;

    public CheckerReconciliationGate(ReconciliationChecker checker) {
        this.checker = checker;
    }

    @Override
    public boolean check(MigrationRun run, List<String> migratedTenantIds) {
        int tenantCount = migratedTenantIds != null ? migratedTenantIds.size() : 0;
        try {
            boolean pass = checker.consistent(run, migratedTenantIds);
            log.info("reconciliation gate: source={}, target={}, migratedTenants={}, pass={}",
                    run.getSourceRegion(), run.getTargetRegion(), tenantCount, pass);
            return pass;
        } catch (RuntimeException e) {
            // 业务校验抛异常视为不通过，避免业务 bug 导致错误切流
            log.warn("reconciliation checker threw exception, treating as NOT pass: {}", e.getMessage(), e);
            return false;
        }
    }
}
