package org.example.migration.shell;

import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.TenantMigrationTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 迁移任务注册表。业务插件以 Spring Bean 形式注册，
 * 命令通过 taskName 查找对应的迁移任务与切流动作。
 *
 * 切流动作为可选——未注册时返回 NoOp，迁移仍可执行（无踢登录）。
 */
public class TaskRegistry {

    private final Map<String, TenantMigrationTask> tasks = new HashMap<>();
    private final Map<String, CutoverAction> cutoverActions = new HashMap<>();

    /** 注册迁移任务（key = task.taskName()） */
    public void register(TenantMigrationTask task) {
        tasks.put(task.taskName(), task);
    }

    /** 为指定 taskName 注册切流动作 */
    public void registerCutover(String taskName, CutoverAction action) {
        cutoverActions.put(taskName, action);
    }

    /** 按 taskName 查找迁移任务，未找到抛异常（含可用 task 提示） */
    public TenantMigrationTask findTask(String taskName) {
        TenantMigrationTask task = tasks.get(taskName);
        if (task == null) {
            throw new IllegalArgumentException(
                    "task not found: " + taskName + ", available tasks: " + tasks.keySet());
        }
        return task;
    }

    /** 按 taskName 查找切流动作，未注册返回 NoOp（迁移仍可跑） */
    public CutoverAction findCutover(String taskName) {
        return cutoverActions.getOrDefault(taskName, NoOpCutoverAction.INSTANCE);
    }

    /** 列出所有已注册的 taskName */
    public List<String> listTaskNames() {
        return tasks.keySet().stream().sorted().collect(Collectors.toList());
    }

    /** 空切流动作：未注册 CutoverAction 时的兜底，避免 NPE */
    static class NoOpCutoverAction implements CutoverAction {
        static final NoOpCutoverAction INSTANCE = new NoOpCutoverAction();

        @Override
        public void evict(org.example.migration.spi.MigrationContext ctx,
                          java.util.List<String> tenantIds, String product, String bizLine) {
            // NoOp：不执行任何切流动作
        }
    }
}
