package org.example.migration.shell;

import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.RegionName;
import org.example.migration.engine.AlwaysPassReconciliationGate;
import org.example.migration.engine.CountReconciliationGate;
import org.example.migration.engine.JdbcCheckpointStore;
import org.example.migration.engine.MigrationEngine;
import org.example.migration.engine.MigrationNotifier;
import org.example.migration.engine.MigrationRequest;
import org.example.migration.engine.ReconciliationCounter;
import org.example.migration.engine.TenantScanner;
import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.result.VerifyResult;
import org.example.migration.client.RegionClientRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

/**
 * 迁移框架命令集。Spring Shell 入口，薄胶水层——解析参数、装配引擎、调用。
 *
 * 命令清单：
 *   migrate       正向迁移（自动扫描租户或手动指定）
 *   resume        从断点续传
 *   rollback      逆向回滚（方向对调复用 migrate）
 *   verify        对账（调用业务插件 verify 钩子）
 *   status        查询迁移状态
 *   tasks         列出已注册业务插件
 *   dry-run       预估待迁移租户数（不写数据）
 */
@Command
@Component
public class MigrationCommands {

    private final TaskRegistry taskRegistry;
    private final DataSource stateDataSource;
    private final RegionClientRegistry clientRegistry;
    private final MigrationProperties migrationProperties;
    private final MigrationNotifier notifier;
    private final ObjectProvider<ReconciliationCounter> counterProvider;
    private final TenantScanner tenantScanner;

    public MigrationCommands(TaskRegistry taskRegistry,
                             DataSource stateDataSource,
                             RegionClientRegistry clientRegistry,
                             MigrationProperties migrationProperties,
                             MigrationNotifier notifier,
                             ObjectProvider<ReconciliationCounter> counterProvider,
                             TenantScanner tenantScanner) {
        this.taskRegistry = taskRegistry;
        this.stateDataSource = stateDataSource;
        this.clientRegistry = clientRegistry;
        this.migrationProperties = migrationProperties;
        this.notifier = notifier;
        this.counterProvider = counterProvider;
        this.tenantScanner = tenantScanner;
    }

    @Command(command = "migrate", description = "正向迁移：扫租户→搬数据→总量闸门→切流")
    public String migrate(
            @Option(shortNames = 't', longNames = "task", description = "任务名") String taskName,
            @Option(shortNames = 's', longNames = "source", description = "源区域") String source,
            @Option(longNames = "target", description = "目标区域") String target,
            @Option(longNames = "product", description = "产品标识", defaultValue = "") String product,
            @Option(longNames = "biz-line", description = "业务线标识", defaultValue = "") String bizLine,
            @Option(longNames = "tenants", description = "租户ID列表,逗号分隔;不填则自动扫描源区") String tenants,
            @Option(longNames = "batch-size", description = "分批大小", defaultValue = "50") int batchSize,
            @Option(longNames = "threads", description = "并发线程数", defaultValue = "4") int threads
    ) {
        var task = taskRegistry.findTask(taskName);
        List<String> tenantIds = resolveTenants(source, tenants);

        MigrationEngine engine = buildEngine(taskName);
        MigrationRequest request = new MigrationRequest(
                taskName, RegionName.of(source), RegionName.of(target),
                product, bizLine, tenantIds, batchSize, threads);

        String runId = engine.migrate(task, request);
        return "Migration completed. runId=" + runId + ", status=" + engineStatus(runId);
    }

    @Command(command = "resume", description = "从断点续传：只处理 PENDING 租户")
    public String resume(
            @Option(longNames = "run-id") String runId,
            @Option(shortNames = 't', longNames = "task") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        MigrationEngine engine = buildEngine(taskName);
        engine.resume(task, runId);
        return "Resume completed. runId=" + runId + ", status=" + engineStatus(runId);
    }

    @Command(command = "rollback", description = "逆向回滚：方向对调复用 migrate")
    public String rollback(
            @Option(longNames = "run-id") String runId,
            @Option(shortNames = 't', longNames = "task") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        MigrationEngine engine = buildEngine(taskName);
        String rollbackRunId = engine.rollback(task, runId);
        return "Rollback completed. rollbackRunId=" + rollbackRunId + ", status=" + engineStatus(rollbackRunId);
    }

    @Command(command = "verify", description = "对账：调用业务插件 verify 钩子")
    public String verify(
            @Option(longNames = "run-id") String runId,
            @Option(shortNames = 't', longNames = "task") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        JdbcCheckpointStore store = new JdbcCheckpointStore(stateDataSource);
        var run = store.findRun(runId);
        if (run == null) {
            return "run not found: " + runId;
        }
        List<String> doneTenants = store.findTenantIdsByStatus(runId, org.example.migration.domain.TenantStatus.DONE);
        MigrationContext ctx = new SimpleContext(run.getSourceRegion(), run.getTargetRegion(), clientRegistry, migrationProperties);
        VerifyResult result = task.verify(ctx, doneTenants, run.getProduct(), run.getBizLine());
        return "Verify runId=" + runId + ", passed=" + result.isPassed()
                + ", checked=" + result.getCheckedCount() + ", mismatch=" + result.getMismatchCount();
    }

    @Command(command = "status", description = "查询迁移状态")
    public String status(@Option(longNames = "run-id") String runId) {
        if (runId == null || runId.isBlank()) {
            return "usage: status --run-id <id>";
        }
        JdbcCheckpointStore store = new JdbcCheckpointStore(stateDataSource);
        var run = store.findRun(runId);
        if (run == null) {
            return "run not found: " + runId;
        }
        return String.format("runId=%s, task=%s, direction=%s, %s→%s, status=%s, processed=%d, failed=%d",
                run.getRunId(), run.getTaskName(), run.getDirection(),
                run.getSourceRegion(), run.getTargetRegion(), run.getStatus(),
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

    @Command(command = "dry-run", description = "预估待迁移租户数(不写数据)")
    public String dryRun(
            @Option(shortNames = 's', longNames = "source") String source,
            @Option(longNames = "target") String target,
            @Option(longNames = "tenants", description = "租户ID列表;不填则扫描源区") String tenants
    ) {
        List<String> tenantIds = resolveTenants(source, tenants);
        return String.format("dry-run: source=%s, target=%s, tenantCount=%d", source, target, tenantIds.size());
    }

    /** 解析租户：手动指定优先,否则扫描源区 */
    private List<String> resolveTenants(String source, String tenants) {
        if (tenants != null && !tenants.isBlank()) {
            return Arrays.stream(tenants.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        if (tenantScanner == null) {
            throw new IllegalArgumentException(
                    "no tenants specified and no TenantScanner configured; use --tenants or configure scanner");
        }
        MigrationContext ctx = new SimpleContext(RegionName.of(source), RegionName.of(source),
                clientRegistry, migrationProperties);
        return tenantScanner.scanSourceTenants(ctx);
    }

    private MigrationEngine buildEngine(String taskName) {
        CutoverAction cutover = taskRegistry.findCutover(taskName);
        ReconciliationCounter counter = counterProvider.getIfAvailable();
        var gate = counter != null
                ? new CountReconciliationGate(counter)
                : new AlwaysPassReconciliationGate();
        return new MigrationEngine(
                new JdbcCheckpointStore(stateDataSource), gate, cutover,
                clientRegistry, migrationProperties, notifier);
    }

    private String engineStatus(String runId) {
        return new JdbcCheckpointStore(stateDataSource).findRun(runId).getStatus().name();
    }

    /** 简化上下文：命令侧构造,提供给 scanner/verify */
    private record SimpleContext(RegionName sourceRegion, RegionName targetRegion,
                                 RegionClientRegistry registry, MigrationProperties config)
            implements MigrationContext {
        @Override
        public <C extends org.example.migration.client.RegionClient> C client(
                RegionName region, org.example.migration.domain.ClientType type, Class<C> clazz) {
            return registry.client(region, type, clazz);
        }

        @Override
        public <C extends org.example.migration.client.RegionClient> C client(
                RegionName region, org.example.migration.domain.ClientType type, String instance, Class<C> clazz) {
            return registry.client(region, type, instance, clazz);
        }
    }
}
