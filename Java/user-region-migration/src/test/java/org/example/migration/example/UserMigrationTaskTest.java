package org.example.migration.example;

import org.example.migration.client.MySqlClient;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.MigrationPhase;
import org.example.migration.domain.RegionName;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.result.MigrationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserMigrationTask 测试：验证两阶段迁移逻辑（ADR-0005）。
 * CORE 分支搬 users 表；SECONDARY 分支搬 user_activity 表。两者均按"读源→写目标→删源"。
 */
class UserMigrationTaskTest {

    private MigrationContext ctxWith(MySqlClient source, MySqlClient target) {
        MigrationContext ctx = mock(MigrationContext.class);
        when(ctx.sourceRegion()).thenReturn(RegionName.SINGAPORE);
        when(ctx.targetRegion()).thenReturn(RegionName.MYANMAR);
        when(ctx.client(RegionName.SINGAPORE, ClientType.MYSQL, "business", MySqlClient.class)).thenReturn(source);
        when(ctx.client(RegionName.MYANMAR, ClientType.MYSQL, "business", MySqlClient.class)).thenReturn(target);
        return ctx;
    }

    @Test
    @DisplayName("CORE: 从源读、写目标、删源,返回成功")
    void shouldReadSourceWriteTargetDeleteSource() {
        MySqlClient source = mock(MySqlClient.class);
        MySqlClient target = mock(MySqlClient.class);
        when(source.queryByTenants(any(), anyList())).thenAnswer(inv -> List.of(new Object()));

        UserMigrationTask task = new UserMigrationTask();
        MigrationResult result = task.migrate(ctxWith(source, target), List.of("t1"), "p", "b", MigrationPhase.CORE);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMigratedCount()).isEqualTo(1);
        verify(source).deleteByTenants(any(), anyList());
    }

    @Test
    @DisplayName("CORE: 源区无数据时返回成功且迁移数为 0")
    void shouldReturnZeroWhenNoData() {
        MySqlClient source = mock(MySqlClient.class);
        MySqlClient target = mock(MySqlClient.class);
        when(source.queryByTenants(any(), anyList())).thenAnswer(inv -> List.of());

        UserMigrationTask task = new UserMigrationTask();
        MigrationResult result = task.migrate(ctxWith(source, target), List.of("t1"), "p", "b", MigrationPhase.CORE);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMigratedCount()).isZero();
    }

    @Test
    @DisplayName("SECONDARY: 搬次核心数据并删源")
    void shouldMigrateSecondaryData() {
        MySqlClient source = mock(MySqlClient.class);
        MySqlClient target = mock(MySqlClient.class);
        when(source.queryByTenants(any(), anyList())).thenAnswer(inv -> List.of(new Object(), new Object()));

        UserMigrationTask task = new UserMigrationTask();
        MigrationResult result = task.migrate(ctxWith(source, target), List.of("t1"), "p", "b",
                MigrationPhase.SECONDARY);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMigratedCount()).isEqualTo(2);
        verify(source).deleteByTenants(any(), anyList());
    }

    @Test
    @DisplayName("taskName 返回 user-migration")
    void shouldReturnTaskName() {
        assertThat(new UserMigrationTask().taskName()).isEqualTo("user-migration");
    }
}
