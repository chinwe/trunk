package org.example.migration.shell;

import org.example.migration.engine.RecordingCutoverAction;
import org.example.migration.engine.FakeMigrationTask;
import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.TenantMigrationTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ShellAutoConfiguration 测试：验证 TaskRegistry 的自动收集逻辑。
 */
class ShellAutoConfigurationTest {

    @Test
    @DisplayName("taskRegistry Bean 自动收集所有 TenantMigrationTask + CutoverAction")
    void shouldCollectTasksAndCutoverActions() {
        TenantMigrationTask task1 = new FakeMigrationTask("user-migration");
        TenantMigrationTask task2 = new FakeMigrationTask("device-migration");
        CutoverAction cutover = new RecordingCutoverAction();

        ShellAutoConfiguration config = new ShellAutoConfiguration();
        TaskRegistry registry = config.taskRegistry(List.of(task1, task2), Map.of("user-migration", cutover));

        assertThat(registry.listTaskNames()).containsExactly("device-migration", "user-migration");
        assertThat(registry.findCutover("user-migration")).isSameAs(cutover);
    }
}
