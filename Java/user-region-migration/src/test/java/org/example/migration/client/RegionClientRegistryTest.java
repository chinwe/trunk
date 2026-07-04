package org.example.migration.client;

import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 客户端注册表行为测试：验证三元组 (region, type, instance) 存储与查表。
 *
 * 测试公共接口（register + client 重载 + listInstances），不关心内部 Map 结构。
 */
class RegionClientRegistryTest {

    @Test
    @DisplayName("三参 register 后三参 client 能取回（instance 占位 default）")
    void shouldRetrieveByThreeParamApi() {
        var registry = new RegionClientRegistry();
        MySqlClient fakeMysql = new FakeMySqlClient();

        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, fakeMysql);

        MySqlClient retrieved = registry.client(RegionName.SINGAPORE, ClientType.MYSQL, MySqlClient.class);
        assertThat(retrieved).isSameAs(fakeMysql);
    }

    @Test
    @DisplayName("四参 register 后四参 client 能取回同一客户端实例")
    void shouldRetrieveByFourParamApi() {
        var registry = new RegionClientRegistry();
        MySqlClient businessMysql = new FakeMySqlClient();
        MySqlClient openMysql = new FakeMySqlClient();

        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "business", businessMysql);
        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "open", openMysql);

        assertThat(registry.client(RegionName.SINGAPORE, ClientType.MYSQL, "business", MySqlClient.class))
                .isSameAs(businessMysql);
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.MYSQL, "open", MySqlClient.class))
                .isSameAs(openMysql);
    }

    @Test
    @DisplayName("同一 region+type 多个实例互不覆盖")
    void shouldNotOverwriteDifferentInstances() {
        var registry = new RegionClientRegistry();
        MySqlClient businessMysql = new FakeMySqlClient();
        MySqlClient openMysql = new FakeMySqlClient();

        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "business", businessMysql);
        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "open", openMysql);

        // business 仍然是第一个注册的实例，没被 open 覆盖
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.MYSQL, "business", MySqlClient.class))
                .isSameAs(businessMysql);
    }

    @Test
    @DisplayName("不同 region 的同类型客户端互不干扰")
    void shouldIsolateClientsByRegion() {
        var registry = new RegionClientRegistry();
        MySqlClient sgClient = new FakeMySqlClient();
        MySqlClient mmClient = new FakeMySqlClient();

        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, sgClient);
        registry.register(RegionName.MYANMAR, ClientType.MYSQL, mmClient);

        assertThat(registry.client(RegionName.SINGAPORE, ClientType.MYSQL, MySqlClient.class)).isSameAs(sgClient);
        assertThat(registry.client(RegionName.MYANMAR, ClientType.MYSQL, MySqlClient.class)).isSameAs(mmClient);
    }

    @Test
    @DisplayName("四参 client 指定不存在的实例名抛 IllegalArgumentException 并列出可用实例")
    void shouldThrowWhenInstanceNotFound() {
        var registry = new RegionClientRegistry();
        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "business", new FakeMySqlClient());
        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "open", new FakeMySqlClient());

        assertThatThrownBy(() ->
                registry.client(RegionName.SINGAPORE, ClientType.MYSQL, "nonexistent", MySqlClient.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nonexistent")
                .hasMessageContaining("business")
                .hasMessageContaining("open");
    }

    @Test
    @DisplayName("三参 client 查询未注册的 region+type 也抛 IllegalArgumentException")
    void shouldThrowWhenThreeParamNotFound() {
        var registry = new RegionClientRegistry();

        assertThatThrownBy(() ->
                registry.client(RegionName.MYANMAR, ClientType.MYSQL, MySqlClient.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MYANMAR")
                .hasMessageContaining("MYSQL")
                .hasMessageContaining("default");
    }

    @Test
    @DisplayName("listInstances 返回某 (region, type) 下已注册的所有实例名")
    void shouldListRegisteredInstances() {
        var registry = new RegionClientRegistry();
        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "business", new FakeMySqlClient());
        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, "open", new FakeMySqlClient());
        registry.register(RegionName.SINGAPORE, ClientType.REDIS, "session", new FakeRedisClient());

        assertThat(registry.listInstances(RegionName.SINGAPORE, ClientType.MYSQL))
                .containsExactly("business", "open");
        assertThat(registry.listInstances(RegionName.SINGAPORE, ClientType.REDIS))
                .containsExactly("session");
    }

    @Test
    @DisplayName("未注册任何实例时 listInstances 返回空列表")
    void shouldReturnEmptyListWhenNoInstances() {
        var registry = new RegionClientRegistry();

        assertThat(registry.listInstances(RegionName.SINGAPORE, ClientType.MYSQL)).isEmpty();
    }

    /** 测试用 fake：实现 MySqlClient 公共接口,仅作占位（registry 测试不关心具体操作） */
    static class FakeMySqlClient implements MySqlClient {
        @Override public Object raw() { return null; }
        @Override public java.util.List<?> queryByTenants(String sql, java.util.List<String> tenantIds) { return java.util.List.of(); }
        @Override public int[] batchUpdate(String sql, java.util.List<Object[]> argsList) { return new int[0]; }
        @Override public int deleteByTenants(String sql, java.util.List<String> tenantIds) { return 0; }
    }

    /** 测试用 fake：实现 RedisClient 公共接口 */
    static class FakeRedisClient implements RedisClient {
        @Override public Object raw() { return null; }
        @Override public java.util.List<String> scanKeysByTenants(String pattern, java.util.List<String> tenantIds) { return java.util.List.of(); }
        @Override public String get(String key) { return null; }
        @Override public void set(String key, String value) { }
        @Override public void delete(String key) { }
    }
}
