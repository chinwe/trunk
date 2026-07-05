package org.example.migration.engine;

import org.example.migration.client.MySqlClient;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.example.migration.spi.MigrationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TenantScanner.MySqlTenantScanner 测试。mock MySqlClient.queryForList（D4：不再依赖 raw() 强转）。
 */
class TenantScannerTest {

    @Test
    @DisplayName("扫描源区 tenant 表,返回所有租户ID")
    void shouldScanAllTenantIds() {
        MySqlClient mysql = mock(MySqlClient.class);
        when(mysql.queryForList("SELECT id FROM tenants")).thenReturn(List.of(
                Map.of("id", "t1"),
                Map.of("id", "t2"),
                Map.of("id", "t3")
        ));

        MigrationContext ctx = mock(MigrationContext.class);
        when(ctx.sourceRegion()).thenReturn(RegionName.SINGAPORE);
        when(ctx.client(RegionName.SINGAPORE, ClientType.MYSQL, MySqlClient.class)).thenReturn(mysql);

        MySqlTenantScanner scanner = new MySqlTenantScanner("tenants");
        List<String> tenantIds = scanner.scanSourceTenants(ctx);

        assertThat(tenantIds).containsExactly("t1", "t2", "t3");
    }

    @Test
    @DisplayName("空表返回空列表")
    void shouldReturnEmptyForEmptyTable() {
        MySqlClient mysql = mock(MySqlClient.class);
        when(mysql.queryForList("SELECT id FROM tenants")).thenReturn(List.of());

        MigrationContext ctx = mock(MigrationContext.class);
        when(ctx.sourceRegion()).thenReturn(RegionName.SINGAPORE);
        when(ctx.client(RegionName.SINGAPORE, ClientType.MYSQL, MySqlClient.class)).thenReturn(mysql);

        MySqlTenantScanner scanner = new MySqlTenantScanner("tenants");
        List<String> tenantIds = scanner.scanSourceTenants(ctx);

        assertThat(tenantIds).isEmpty();
    }
}
