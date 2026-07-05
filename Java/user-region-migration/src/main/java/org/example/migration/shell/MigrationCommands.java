package org.example.migration.shell;

import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.RegionName;
import org.example.migration.engine.AlwaysPassReconciliationGate;
import org.example.migration.engine.CheckerReconciliationGate;
import org.example.migration.engine.CheckpointStore;
import org.example.migration.engine.MigrationEngine;
import org.example.migration.engine.MigrationNotifier;
import org.example.migration.engine.MigrationRequest;
import org.example.migration.engine.ReconciliationChecker;
import org.example.migration.engine.RegistryMigrationContext;
import org.example.migration.engine.TenantScanner;
import org.example.migration.engine.TokenBucketRateLimiter;
import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.result.VerifyResult;
import org.example.migration.client.RegionClientRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 迁移框架命令集。Spring Shell 入口，薄胶水层——解析参数、装配引擎、调用。
 *
 * 命令清单：
 *   migrate       正向迁移（两阶段，ADR-0005：CORE 切流后继续 SECONDARY）
 *   resume        从断点续传（按 Run 状态分发 CORE/SECONDARY）
 *   rollback      逆向回滚（方向对调复用 migrate；已切流态拒绝）
 *   verify        对账（调用业务插件 verify 钩子）
 *   status        查询迁移状态
 *   tasks         列出已注册业务插件
 *   dry-run       预估待迁移租户数与两阶段编排（不写数据）
 */
@Command
@Component
public class MigrationCommands {

    private final TaskRegistry taskRegistry;
    private final CheckpointStore store;
    private final RegionClientRegistry clientRegistry;
    private final MigrationProperties migrationProperties;
    private final MigrationNotifier notifier;
    private final TokenBucketRateLimiter rateLimiter;
    private final ObjectProvider<ReconciliationChecker> checkerProvider;
    private final TenantScanner tenantScanner;

    public MigrationCommands(TaskRegistry taskRegistry,
                             CheckpointStore store,
                             RegionClientRegistry clientRegistry,
                             MigrationProperties migrationProperties,
                             MigrationNotifier notifier,
                             TokenBucketRateLimiter rateLimiter,
                             ObjectProvider<ReconciliationChecker> checkerProvider,
                             TenantScanner tenantScanner) {
        this.taskRegistry = taskRegistry;
        this.store = store;
        this.clientRegistry = clientRegistry;
        this.migrationProperties = migrationProperties;
        this.notifier = notifier;
        this.rateLimiter = rateLimiter;
        this.checkerProvider = checkerProvider;
        this.tenantScanner = tenantScanner;
    }

    @Command(command = "migrate", description = "正向迁移（两阶段）：CORE→切流→SECONDARY")
    public String migrate(
            @Option(shortNames = 't', longNames = "task", description = "任务名") String taskName,
            @Option(shortNames = 's', longNames = "source", description = "源区域") String source,
            @Option(longNames = "target", description = "目标区域") String target,
            @Option(longNames = "product", description = "产品标识", defaultValue = "") String product,
            @Option(longNames = "biz-line", description = "业务线标识", defaultValue = "") String bizLine,
            @Option(longNames = "tenants", description = "租户ID列表,逗号分隔;不填则自动扫描源区") String tenants,
            @Option(longNames = "batch-size", description = "进度汇报粒度", defaultValue = "50") int batchSize,
            @Option(longNames = "threads", description = "并发线程数", defaultValue = "4") int threads
    ) {
        var task = taskRegistry.findTask(taskName);
        List<String> tenantIds = resolveTenants(source, target, tenants);

        MigrationEngine engine = buildEngine(taskName);
        MigrationRequest request = new MigrationRequest(
                taskName, RegionName.of(source), RegionName.of(target),
                product, bizLine, tenantIds, batchSize, threads);

        String runId = engine.migrate(task, request);
        return "Migration completed. runId=" + runId + ", status=" + engineStatus(runId);
    }

    @Command(command = "resume", description = "从断点续传：按 Run 状态分发（CORE/SECONDARY）")
    public String resume(
            @Option(longNames = "run-id") String runId,
            @Option(shortNames = 't', longNames = "task") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        MigrationEngine engine = buildEngine(taskName);
        engine.resume(task, runId);
        return "Resume completed. runId=" + runId + ", status=" + engineStatus(runId);
    }

    @Command(command = "rollback", description = "逆向回滚：方向对调复用 migrate（已切流态拒绝）")
    public String rollback(
            @Option(longNames = "run-id") String runId,
            @Option(shortNames = 't', longNames = "task") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        MigrationEngine engine = buildEngine(taskName);
        String rollbackRunId;
        try {
            rollbackRunId = engine.rollback(task, runId);
        } catch (IllegalStateException e) {
            // 已切流态拒绝 rollback（ADR-0005 Q6）——命令层捕获返回友好消息
            return "Rollback rejected: " + e.getMessage();
        }

        // 提示运维：原 run 的 FAILED 租户不会被框架回滚（业务 migrate 抛异常前可能已部分写入）
        int forwardFailed = store.findTenantIdsByStatus(runId,
                org.example.migration.domain.TenantStatus.FAILED).size();
        String suffix = forwardFailed > 0
                ? ". WARNING: " + forwardFailed + " FAILED tenant(s) in forward run NOT rolled back by framework"
                : "";
        return "Rollback completed. rollbackRunId=" + rollbackRunId + ", status=" + engineStatus(rollbackRunId) + suffix;
    }

    @Command(command = "verify", description = "对账：调用业务插件 verify 钩子")
    public String verify(
            @Option(longNames = "run-id") String runId,
            @Option(shortNames = 't', longNames = "task") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        var run = store.findRun(runId);
        if (run == null) {
            return "run not found: " + runId;
        }
        List<String> doneTenants = store.findTenantIdsByStatus(runId, org.example.migration.domain.TenantStatus.DONE);
        MigrationContext ctx = new RegistryMigrationContext(
                run.getSourceRegion(), run.getTargetRegion(), clientRegistry, migrationProperties);
        VerifyResult result = task.verify(ctx, doneTenants, run.getProduct(), run.getBizLine());
        return "Verify runId=" + runId + ", passed=" + result.isPassed()
                + ", checked=" + result.getCheckedCount() + ", mismatch=" + result.getMismatchCount();
    }

    @Command(command = "status", description = "查询迁移状态")
    public String status(@Option(longNames = "run-id") String runId) {
        if (runId == null || runId.isBlank()) {
            return "usage: status --run-id <id>";
        }
        var run = store.findRun(runId);
        if (run == null) {
            return "run not found: " + runId;
        }
        return String.format("runId=%s, task=%s, direction=%s, %s→%s, status=%s, phase=%s, processed=%d, failed=%d",
                run.getRunId(), run.getTaskName(), run.getDirection(),
                run.getSourceRegion(), run.getTargetRegion(), run.getStatus(), run.getPhase(),
                run.getProcessedTenants(), run.getFailedTenants());
    }

    @Command(command = "tasks", description = "列出已注册的业务插件")
    public String tasks() {
        List<String> names = taskRegistry.listTaskNames();
        if (names.isEmpty()) {
            return "no tasks registered";
        }
        return "registered tasks: " + names;
    }

    @Command(command = "dry-run", description = "预估待迁移租户数与两阶段编排(不写数据)")
    public String dryRun(
            @Option(shortNames = 's', longNames = "source") String source,
            @Option(longNames = "target") String target,
            @Option(longNames = "tenants", description = "租户ID列表;不填则扫描源区") String tenants
    ) {
        List<String> tenantIds = resolveTenants(source, target, tenants);
        // 演示两阶段编排（ADR-0005）：CORE → 切流 → SECONDARY。不真跑。
        return String.format(
                "dry-run: source=%s, target=%s, tenantCount=%d%n" +
                "  phase[CORE]: migrate core data → reconcile(CORE) → cutover → notify[phase=CORE_CUTOVER]%n" +
                "  phase[SECONDARY]: migrate secondary data → reconcile(SECONDARY) → notify[phase=ALL_DONE]",
                source, target, tenantIds.size());
    }

    /** 解析租户：手动指定优先，否则扫描源区 */
    private List<String> resolveTenants(String source, String target, String tenants) {
        if (tenants != null && !tenants.isBlank()) {
            return Arrays.stream(tenants.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        if (tenantScanner == null) {
            throw new IllegalArgumentException(
                    "no tenants specified and no TenantScanner configured; use --tenants or configure scanner");
        }
        MigrationContext ctx = new RegistryMigrationContext(
                RegionName.of(source), RegionName.of(target), clientRegistry, migrationProperties);
        return tenantScanner.scanSourceTenants(ctx);
    }

    private MigrationEngine buildEngine(String taskName) {
        CutoverAction cutover = taskRegistry.findCutover(taskName);
        ReconciliationChecker checker = checkerProvider.getIfAvailable();
        var gate = checker != null
                ? new CheckerReconciliationGate(checker)
                : new AlwaysPassReconciliationGate();
        return MigrationEngine.builder(store, gate, cutover)
                .registry(clientRegistry)
                .properties(migrationProperties)
                .notifier(notifier)
                .rateLimiter(rateLimiter)  // 进程级单例，多 run 共享（R3）
                .build();
    }

    private String engineStatus(String runId) {
        return store.findRun(runId).getStatus().name();
    }
}
