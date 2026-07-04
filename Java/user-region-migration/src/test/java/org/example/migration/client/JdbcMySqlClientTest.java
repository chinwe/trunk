package org.example.migration.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JdbcMySqlClient 测试。用 Mockito mock JdbcTemplate，验证 sql 展开与委托调用。
 */
class JdbcMySqlClientTest {

    @Nested
    @DisplayName("IN 占位符展开")
    class InPlaceholderExpansion {

        @Test
        @DisplayName("多个租户时,:tenants 展开为对应数量的 ? 占位符")
        void shouldExpandToMultiplePlaceholders() {
            String sql = "SELECT * FROM users WHERE tenant_id IN (:tenants)";

            String result = JdbcMySqlClient.expandInPlaceholder(sql, 3);

            assertThat(result).isEqualTo("SELECT * FROM users WHERE tenant_id IN (?,?,?)");
        }

        @Test
        @DisplayName("单个租户时展开为一个 ? 占位符")
        void shouldExpandSinglePlaceholder() {
            String sql = "DELETE FROM users WHERE tenant_id IN (:tenants)";

            String result = JdbcMySqlClient.expandInPlaceholder(sql, 1);

            assertThat(result).isEqualTo("DELETE FROM users WHERE tenant_id IN (?)");
        }

        @Test
        @DisplayName("零个租户时展开为 NULL(避免空 IN 语法错误)")
        void shouldExpandToNullWhenEmpty() {
            String sql = "SELECT * FROM users WHERE tenant_id IN (:tenants)";

            String result = JdbcMySqlClient.expandInPlaceholder(sql, 0);

            assertThat(result).isEqualTo("SELECT * FROM users WHERE tenant_id IN (NULL)");
        }
    }

    @Nested
    @DisplayName("委托 JdbcTemplate")
    class Delegation {

        @Test
        @DisplayName("queryByTenants 调用 JdbcTemplate.queryForList 并展开占位符")
        void shouldDelegateQueryToJdbcTemplate() {
            JdbcTemplate jdbc = mock(JdbcTemplate.class);
            when(jdbc.queryForList(eq("SELECT * FROM t WHERE id IN (?,?)"), eq(new Object[]{"t1", "t2"})))
                    .thenReturn(List.of(Map.of("id", "t1")));

            JdbcMySqlClient client = new JdbcMySqlClient(jdbc);
            List<?> result = client.queryByTenants("SELECT * FROM t WHERE id IN (:tenants)", List.of("t1", "t2"));

            assertThat(result).hasSize(1);
            verify(jdbc).queryForList("SELECT * FROM t WHERE id IN (?,?)", new Object[]{"t1", "t2"});
        }

        @Test
        @DisplayName("deleteByTenants 调用 JdbcTemplate.update 并展开占位符")
        void shouldDelegateDeleteToJdbcTemplate() {
            JdbcTemplate jdbc = mock(JdbcTemplate.class);
            when(jdbc.update(eq("DELETE FROM t WHERE id IN (?)"), eq(new Object[]{"t1"}))).thenReturn(1);

            JdbcMySqlClient client = new JdbcMySqlClient(jdbc);
            int affected = client.deleteByTenants("DELETE FROM t WHERE id IN (:tenants)", List.of("t1"));

            assertThat(affected).isEqualTo(1);
        }

        @Test
        @DisplayName("raw() 返回底层 JdbcTemplate")
        void shouldReturnRawJdbcTemplate() {
            JdbcTemplate jdbc = mock(JdbcTemplate.class);
            JdbcMySqlClient client = new JdbcMySqlClient(jdbc);

            assertThat(client.raw()).isSameAs(jdbc);
        }
    }
}
