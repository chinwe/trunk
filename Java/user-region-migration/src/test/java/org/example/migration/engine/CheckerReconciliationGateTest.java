package org.example.migration.engine;

import org.example.migration.domain.Direction;
import org.example.migration.domain.MigrationPhase;
import org.example.migration.domain.RegionName;
import org.example.migration.domain.RunStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CheckerReconciliationGate 测试：验证业务自证一致性的委托与异常容错（ADR-0001）。
 *
 * <p>这是 C1 修复后的对账闸门——算法由业务 {@link ReconciliationChecker} 自证，
 * 框架只接受二值判定。业务 checker 抛异常时视为不通过（避免业务 bug 导致错误切流）。
 *
 * <p>两阶段（ADR-0005）：checker.consistent 接收 phase 参数；gate 从 run.getPhase() 透传。
 */
class CheckerReconciliationGateTest {

    private MigrationRun runBetween(RegionName source, RegionName target) {
        MigrationRun run = new MigrationRun();
        run.setRunId("r1");
        run.setSourceRegion(source);
        run.setTargetRegion(target);
        run.setStatus(RunStatus.RUNNING_CORE);
        run.setPhase(MigrationPhase.CORE);
        run.setDirection(Direction.FORWARD);
        return run;
    }

    @Test
    @DisplayName("业务 checker 返回 true 时闸门通过")
    void shouldPassWhenCheckerReturnsTrue() {
        ReconciliationChecker checker = (run, tenants, phase) -> true;
        CheckerReconciliationGate gate = new CheckerReconciliationGate(checker);
        MigrationRun run = runBetween(RegionName.SINGAPORE, RegionName.MYANMAR);

        boolean pass = gate.check(run, List.of("t1", "t2"));

        assertThat(pass).isTrue();
    }

    @Test
    @DisplayName("业务 checker 返回 false 时闸门不通过")
    void shouldFailWhenCheckerReturnsFalse() {
        ReconciliationChecker checker = (r, tenants, phase) -> false;
        CheckerReconciliationGate gate = new CheckerReconciliationGate(checker);
        MigrationRun run = runBetween(RegionName.SINGAPORE, RegionName.MYANMAR);

        boolean pass = gate.check(run, List.of("t1"));

        assertThat(pass).isFalse();
    }

    @Test
    @DisplayName("业务 checker 抛异常时视为不通过(避免业务 bug 导致错误切流)")
    void shouldFailWhenCheckerThrows() {
        ReconciliationChecker checker = (r, tenants, phase) -> {
            throw new RuntimeException("db connection lost");
        };
        CheckerReconciliationGate gate = new CheckerReconciliationGate(checker);
        MigrationRun run = runBetween(RegionName.SINGAPORE, RegionName.MYANMAR);

        boolean pass = gate.check(run, List.of("t1"));

        assertThat(pass).isFalse();
    }
}
