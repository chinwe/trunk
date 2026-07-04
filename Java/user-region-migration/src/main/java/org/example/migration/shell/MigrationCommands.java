package org.example.migration.shell;

import org.example.migration.domain.RegionName;
import org.example.migration.engine.AlwaysPassReconciliationGate;
import org.example.migration.engine.CheckpointStore;
import org.example.migration.engine.InMemoryCheckpointStore;
import org.example.migration.engine.MigrationEngine;
import org.example.migration.engine.MigrationRequest;
import org.example.migration.spi.CutoverAction;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 迁移框架命令集。Spring Shell 入口，薄胶水层——解析参数、装配引擎、调用。
 *
 * 命令清单：
 *   migrate   正向迁移（搬数据→总量闸门→切流）
 *   resume    从断点续传
 *   rollback  逆向回滚（方向对调复用 migrate）
 *   verify    对账
 *   status    查询迁移状态
 *   tasks     列出已注册业务插件
 */
@Command
@Component
public class MigrationCommands {

    private final TaskRegistry taskRegistry;
    /** 状态存储：骨架用内存版，生产应替换为 JdbcCheckpointStore */
    private final CheckpointStore store = new InMemoryCheckpointStore();

    public MigrationCommands(TaskRegistry taskRegistry) {
        this.taskRegistry = taskRegistry;
    }

    @Command(command = "migrate", description = "正向迁移：搬数据 → 总量闸门 → 切流")
    public String migrate(
            @Option(shortNames = 't', longNames = "task", description = "任务名，如 user-migration") String taskName,
            @Option(shortNames = 's', longNames = "source", description = "源区域") String source,
            @Option(longNames = "target", description = "目标区域") String target,
            @Option(longNames = "product", description = "产品标识", defaultValue = "") String product,
            @Option(longNames = "biz-line", description = "业务线标识", defaultValue = "") String bizLine,
            @Option(longNames = "tenants", description = "租户ID列表，逗号分隔") String tenants,
            @Option(longNames = "batch-size", description = "分批大小", defaultValue = "50") int batchSize,
            @Option(longNames = "threads", description = "并发线程数", defaultValue = "4") int threads
    ) {
        var task = taskRegistry.findTask(taskName);
        CutoverAction cutover = taskRegistry.findCutover(taskName);

        MigrationEngine engine = new MigrationEngine(store, new AlwaysPassReconciliationGate(), cutover);
        List<String> tenantIds = parseTenants(tenants);

        MigrationRequest request = new MigrationRequest(
                taskName, RegionName.of(source), RegionName.of(target),
                product, bizLine, tenantIds, batchSize, threads);

        String runId = engine.migrate(task, request);
        return "Migration completed. runId=" + runId + ", status=" + store.findRun(runId).getStatus();
    }

    @Command(command = "resume", description = "从断点续传：只处理 PENDING 租户")
    public String resume(
            @Option(longNames = "run-id", description = "迁移执行ID") String runId,
            @Option(shortNames = 't', longNames = "task", description = "任务名") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        CutoverAction cutover = taskRegistry.findCutover(taskName);
        MigrationEngine engine = new MigrationEngine(store, new AlwaysPassReconciliationGate(), cutover);

        engine.resume(task, runId);
        return "Resume completed. runId=" + runId + ", status=" + store.findRun(runId).getStatus();
    }

    @Command(command = "rollback", description = "逆向回滚：方向对调复用 migrate")
    public String rollback(
            @Option(longNames = "run-id", description = "原正向迁移执行ID") String runId,
            @Option(shortNames = 't', longNames = "task", description = "任务名") String taskName
    ) {
        var task = taskRegistry.findTask(taskName);
        CutoverAction cutover = taskRegistry.findCutover(taskName);
        MigrationEngine engine = new MigrationEngine(store, new AlwaysPassReconciliationGate(), cutover);

        String rollbackRunId = engine.rollback(task, runId);
        return "Rollback completed. rollbackRunId=" + rollbackRunId
                + ", status=" + store.findRun(rollbackRunId).getStatus();
    }

    @Command(command = "status", description = "查询迁移状态")
    public String status(@Option(longNames = "run-id", description = "迁移执行ID") String runId) {
        if (runId == null || runId.isBlank()) {
            return "usage: status --run-id <id>";
        }
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

    private List<String> parseTenants(String tenants) {
        if (tenants == null || tenants.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tenants.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
