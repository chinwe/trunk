package org.example.migration.client;

import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 客户端注册表行为测试：验证能按 (region, type) 注册并查表返回正确客户端。
 * 用实现了公共接口的 fake 客户端，不 mock 内部。
 */
class RegionClientRegistryTest {

    @Test
    @DisplayName("按 region + type 注册后,能查表取回同一客户端实例")
    void shouldRetrieveRegisteredClientByRegionAndType() {
        var registry = new RegionClientRegistry();
        MySqlClient fakeMysql = new FakeMySqlClient();

        registry.register(RegionName.SINGAPORE, ClientType.MYSQL, fakeMysql);

        MySqlClient retrieved = registry.client(RegionName.SINGAPORE, ClientType.MYSQL, MySqlClient.class);
        assertThat(retrieved).isSameAs(fakeMysql);
    }

    @Test
    @DisplayName("未注册的 region+type 查表返回空")
    void shouldReturnNullForUnregisteredCombo() {
        var registry = new RegionClientRegistry();

        MySqlClient retrieved = registry.client(RegionName.MYANMAR, ClientType.MYSQL, MySqlClient.class);

        assertThat(retrieved).isNull();
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

    /** 测试用 fake：实现 MySqlClient 公共接口,仅作占位（registry 测试不关心具体操作） */
    static class FakeMySqlClient implements MySqlClient {
        @Override
        public Object raw() {
            return null;
        }

        @Override
        public java.util.List<?> queryByTenants(String sql, java.util.List<String> tenantIds) {
            return java.util.List.of();
        }

        @Override
        public int[] batchUpdate(String sql, java.util.List<Object[]> argsList) {
            return new int[0];
        }

        @Override
        public int deleteByTenants(String sql, java.util.List<String> tenantIds) {
            return 0;
        }
    }
}
