package org.example.migration.client;

import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端注册表。按 (region, type) 存储并查表返回客户端。
 * 由配置驱动的自动装配在启动时注册各 region 的各中间件客户端实例。
 */
public class RegionClientRegistry {

    /** key = region名 + "|" + client类型名 */
    private final Map<String, RegionClient> clients = new HashMap<>();

    /** 注册一个客户端 */
    public void register(RegionName region, ClientType type, RegionClient client) {
        clients.put(key(region, type), client);
    }

    /**
     * 按 (region, type) 查表并转换为指定子接口类型。
     *
     * @param region 区域
     * @param type   中间件类型
     * @param clazz  期望的子接口类型
     * @return 客户端实例，未注册返回 null
     */
    public <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz) {
        RegionClient client = clients.get(key(region, type));
        if (client == null) {
            return null;
        }
        return clazz.cast(client);
    }

    private String key(RegionName region, ClientType type) {
        return region.value() + "|" + type.name();
    }
}
