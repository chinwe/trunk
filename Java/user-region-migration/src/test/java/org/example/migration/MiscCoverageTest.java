package org.example.migration;

import org.example.migration.config.MigrationInfrastructureConfiguration;
import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.engine.AlwaysPassReconciliationGate;
import org.example.migration.engine.MigrationNotifier;
import org.example.migration.engine.TenantScanner;
import org.example.migration.spi.result.MigrationResult;
import org.example.migration.spi.result.VerifyResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 杂项覆盖率补充：值对象工厂方法、默认 gate、基础设施配置等小缺口的测试。
 */
class MiscCoverageTest {

    @Test
    @DisplayName("MigrationResult.failure 工厂方法")
    void migrationResultFailure() {
        MigrationResult r = MigrationResult.failure("boom");
        assertThat(r.isSuccess()).isFalse();
        assertThat(r.getErrorMessage()).isEqualTo("boom");
    }

    @Test
    @DisplayName("VerifyResult.passed/failed/unimplemented 工厂方法")
    void verifyResultFactories() {
        VerifyResult passed = VerifyResult.passed(10);
        assertThat(passed.isPassed()).isTrue();
        assertThat(passed.getCheckedCount()).isEqualTo(10);

        VerifyResult failed = VerifyResult.failed(10, 2, "detail");
        assertThat(failed.isPassed()).isFalse();
        assertThat(failed.getMismatchCount()).isEqualTo(2);
        assertThat(failed.getDetail()).isEqualTo("detail");

        VerifyResult unimpl = VerifyResult.unimplemented();
        assertThat(unimpl.isPassed()).isTrue();
    }

    @Test
    @DisplayName("AlwaysPassReconciliationGate 总是返回 true")
    void alwaysPassGate() {
        AlwaysPassReconciliationGate gate = new AlwaysPassReconciliationGate();
        assertThat(gate.check(new MigrationRun())).isTrue();
    }

    @Test
    @DisplayName("MigrationNotifier.NO_OP 不抛异常")
    void noOpNotifier() {
        MigrationNotifier.NO_OP.notify(null, null, "k", "p");
    }

    @Test
    @DisplayName("MigrationInfrastructureConfiguration 提供默认 Notifier 与 TenantScanner")
    void infraDefaults() {
        MigrationInfrastructureConfiguration cfg = new MigrationInfrastructureConfiguration();
        TenantScanner scanner = cfg.tenantScanner();
        assertThat(scanner).isNotNull();
    }

    @Test
    @DisplayName("MigrationProperties setter/getter")
    void migrationPropertiesAccessors() {
        MigrationProperties props = new MigrationProperties();
        props.setDefaultBatchSize(20);
        props.setDefaultThreads(8);
        assertThat(props.getDefaultBatchSize()).isEqualTo(20);
        assertThat(props.getDefaultThreads()).isEqualTo(8);
    }
}
