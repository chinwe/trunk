package org.example.migration;

import org.example.migration.config.MigrationInfrastructureConfiguration;
import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.engine.AlwaysPassReconciliationGate;
import org.example.migration.engine.FakeReconciliationGate;
import org.example.migration.engine.InMemoryCheckpointStore;
import org.example.migration.engine.MigrationEngine;
import org.example.migration.engine.MigrationNotifier;
import org.example.migration.engine.RecordingCutoverAction;
import org.example.migration.engine.TenantScanner;
import org.example.migration.engine.TokenBucketRateLimiter;
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
        assertThat(gate.check(new MigrationRun(), java.util.List.of())).isTrue();
    }

    @Test
    @DisplayName("MigrationNotifier.NO_OP 不抛异常")
    void noOpNotifier() {
        MigrationNotifier.NO_OP.notify(null, null, "p");
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

    @Test
    @DisplayName("MigrationEngine.Builder 用默认值构建(无 registry/props)")
    void engineBuilderWithDefaults() {
        InMemoryCheckpointStore store = new InMemoryCheckpointStore();
        MigrationEngine engine = MigrationEngine.builder(
                store, new FakeReconciliationGate(true), new RecordingCutoverAction()).build();
        assertThat(engine).isNotNull();
    }

    @Test
    @DisplayName("MigrationEngine.Builder 显式设所有 optional 依赖后构建")
    void engineBuilderWithAllOptionals() {
        MigrationProperties props = new MigrationProperties();
        props.setDefaultBatchSize(100);
        props.setDefaultThreads(2);
        props.setRateLimitQps(100);
        MigrationProperties.RetryConfig retry = new MigrationProperties.RetryConfig();
        retry.setMaxAttempts(5);
        retry.setBackoffInitial("2s");
        props.setRetry(retry);

        MigrationEngine engine = MigrationEngine.builder(
                new InMemoryCheckpointStore(), new FakeReconciliationGate(true),
                new RecordingCutoverAction())
                .properties(props)
                .notifier(MigrationNotifier.NO_OP)
                .rateLimiter(TokenBucketRateLimiter.noop())
                .build();
        assertThat(engine).isNotNull();
    }

    @Test
    @DisplayName("MigrationEngine.Builder registry 链式调用")
    void engineBuilderWithRegistry() {
        MigrationEngine engine = MigrationEngine.builder(
                new InMemoryCheckpointStore(), new FakeReconciliationGate(true),
                new RecordingCutoverAction())
                .registry(new org.example.migration.client.RegionClientRegistry())
                .build();
        assertThat(engine).isNotNull();
    }

    @Test
    @DisplayName("MigrationProperties 重试配置覆盖")
    void migrationPropertiesRetry() {
        MigrationProperties props = new MigrationProperties();
        props.setRateLimitQps(0); // 0 = 不限流

        MigrationProperties.RetryConfig retry = new MigrationProperties.RetryConfig();
        retry.setMaxAttempts(3);
        retry.setBackoffInitial("500ms");
        props.setRetry(retry);

        assertThat(props.getRateLimitQps()).isZero();
        assertThat(props.getRetry().getMaxAttempts()).isEqualTo(3);
        assertThat(props.getRetry().getBackoffInitial()).isEqualTo("500ms");
    }

    @Test
    @DisplayName("MigrationEngine.Builder parseBackoffMillis 解析各种格式(通过 build 触发)")
    void engineBuilderParseBackoffVariants() {
        // ms 格式
        MigrationProperties p1 = new MigrationProperties();
        MigrationProperties.RetryConfig r1 = new MigrationProperties.RetryConfig();
        r1.setMaxAttempts(3);
        r1.setBackoffInitial("100ms");
        p1.setRetry(r1);
        assertThat(MigrationEngine.builder(
                new InMemoryCheckpointStore(), new FakeReconciliationGate(true),
                new RecordingCutoverAction()).properties(p1).build()).isNotNull();

        // s 格式
        MigrationProperties p2 = new MigrationProperties();
        MigrationProperties.RetryConfig r2 = new MigrationProperties.RetryConfig();
        r2.setMaxAttempts(3);
        r2.setBackoffInitial("1.5s");
        p2.setRetry(r2);
        assertThat(MigrationEngine.builder(
                new InMemoryCheckpointStore(), new FakeReconciliationGate(true),
                new RecordingCutoverAction()).properties(p2).build()).isNotNull();
    }
}
