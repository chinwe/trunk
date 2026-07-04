package org.example.migration.engine;

import org.example.migration.domain.Direction;
import org.example.migration.domain.RegionName;
import org.example.migration.domain.RunStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CountReconciliationGate 测试：验证计数对比行为。
 */
class CountReconciliationGateTest {

    private MigrationRun runBetween(RegionName source, RegionName target) {
        MigrationRun run = new MigrationRun();
        run.setRunId("r1");
        run.setSourceRegion(source);
        run.setTargetRegion(target);
        run.setStatus(RunStatus.RUNNING);
        run.setDirection(Direction.FORWARD);
        return run;
    }

    @Test
    @DisplayName("源与目标计数一致时通过")
    void shouldPassWhenCountsMatch() {
        ReconciliationCounter counter = (region, run) -> 100L;
        CountReconciliationGate gate = new CountReconciliationGate(counter);

        boolean pass = gate.check(runBetween(RegionName.SINGAPORE, RegionName.MYANMAR));

        assertThat(pass).isTrue();
    }

    @Test
    @DisplayName("源与目标计数不一致时不通过")
    void shouldFailWhenCountsMismatch() {
        ReconciliationCounter counter = (region, run) ->
                region.equals(RegionName.SINGAPORE) ? 100L : 99L;
        CountReconciliationGate gate = new CountReconciliationGate(counter);

        boolean pass = gate.check(runBetween(RegionName.SINGAPORE, RegionName.MYANMAR));

        assertThat(pass).isFalse();
    }

    @Test
    @DisplayName("未提供 counter 时默认通过(向后兼容)")
    void shouldPassByDefaultWhenNoCounter() {
        CountReconciliationGate gate = new CountReconciliationGate(null);

        boolean pass = gate.check(runBetween(RegionName.SINGAPORE, RegionName.MYANMAR));

        assertThat(pass).isTrue();
    }
}
