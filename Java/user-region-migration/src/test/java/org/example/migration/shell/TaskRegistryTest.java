package org.example.migration.shell;

import org.example.migration.engine.FakeMigrationTask;
import org.example.migration.engine.RecordingCutoverAction;
import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.TenantMigrationTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TaskRegistry 行为测试：验证能按 taskName 查找迁移任务与切流动作。
 */
class TaskRegistryTest {

    @Test
    @DisplayName("注册的任务能按 taskName 查回")
    void shouldFindRegisteredTaskByName() {
        TaskRegistry registry = new TaskRegistry();
        FakeMigrationTask task = new FakeMigrationTask("user-migration");
        registry.register(task);

        TenantMigrationTask found = registry.findTask("user-migration");

        assertThat(found).isSameAs(task);
    }

    @Test
    @DisplayName("查找未注册的 taskName 抛异常,提示可用的 task")
    void shouldThrowForUnknownTaskName() {
        TaskRegistry registry = new TaskRegistry();
        registry.register(new FakeMigrationTask("user-migration"));

        assertThatThrownBy(() -> registry.findTask("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("user-migration");
    }

    @Test
    @DisplayName("切流动作能按关联 taskName 查回")
    void shouldFindCutoverActionByTaskName() {
        TaskRegistry registry = new TaskRegistry();
        registry.register(new FakeMigrationTask("user-migration"));
        RecordingCutoverAction cutover = new RecordingCutoverAction();
        registry.registerCutover("user-migration", cutover);

        CutoverAction found = registry.findCutover("user-migration");

        assertThat(found).isSameAs(cutover);
    }

    @Test
    @DisplayName("未注册切流动作时返回 NoOp 切流(避免 NPE,迁移仍可跑)")
    void shouldReturnNoOpCutoverWhenNotRegistered() {
        TaskRegistry registry = new TaskRegistry();
        registry.register(new FakeMigrationTask("user-migration"));

        CutoverAction cutover = registry.findCutover("user-migration");

        // 调用不抛异常（NoOp）
        cutover.evict(null, List.of("t1"), "p", "b");
    }

    @Test
    @DisplayName("列出所有已注册的 taskName")
    void shouldListAllRegisteredTaskNames() {
        TaskRegistry registry = new TaskRegistry();
        registry.register(new FakeMigrationTask("user-migration"));
        registry.register(new FakeMigrationTask("device-migration"));

        assertThat(registry.listTaskNames()).containsExactlyInAnyOrder("user-migration", "device-migration");
    }
}
