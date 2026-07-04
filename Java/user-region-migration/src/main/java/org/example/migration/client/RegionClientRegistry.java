package org.example.migration.client;

import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户端注册表。按 (region, type, instance) 存储并查表返回客户端。
 *
 * 纵向多实例：MySQL/Redis 可有多个命名实例（如 business/open、session/cache），
 * 由 instance 维度区分。单实例中间件（ES/S3/DynamoDB/Kafka）走三参重载，
 * instance 固定占位 "default"。
 */
public class RegionClientRegistry {

    /** 单实例中间件的固定 instance 占位名 */
    public static final String DEFAULT_INSTANCE = "default";

    /** key = region名 + "|" + client类型名 + "|" + instance名 */
    private final Map<String, RegionClient> clients = new HashMap<>();

    /** 注册单实例中间件客户端（instance 占位 default） */
    public void register(RegionName region, ClientType type, RegionClient client) {
        register(region, type, DEFAULT_INSTANCE, client);
    }

    /** 注册多实例中间件客户端（MySQL/Redis 用，显式指定 instance 名） */
    public void register(RegionName region, ClientType type, String instance, RegionClient client) {
        clients.put(key(region, type, instance), client);
    }

    /**
     * 按 (region, type) 查表（单实例中间件用）。内部转 instance=default。
     *
     * @return 客户端实例
     * @throws IllegalArgumentException 未注册时抛出
     */
    public <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz) {
        return client(region, type, DEFAULT_INSTANCE, clazz);
    }

    /**
     * 按 (region, type, instance) 查表（MySQL/Redis 用）。
     *
     * @return 客户端实例
     * @throws IllegalArgumentException 指定实例名未注册时抛出,错误信息包含可用实例列表
     */
    public <C extends RegionClient> C client(RegionName region, ClientType type, String instance, Class<C> clazz) {
        RegionClient client = clients.get(key(region, type, instance));
        if (client == null) {
            java.util.List<String> available = listInstances(region, type);
            throw new IllegalArgumentException(
                    "no client registered for region=" + region.value()
                    + ", type=" + type.name() + ", instance=" + instance
                    + (available.isEmpty() ? " (no instances configured for this region+type)"
                       : ". available instances: " + available));
        }
        return clazz.cast(client);
    }

    /**
     * 列出某 (region, type) 下已注册的所有 instance 名。
     * 用于查询未命中时给出可用实例提示。
     */
    public java.util.List<String> listInstances(RegionName region, ClientType type) {
        String prefix = region.value() + "|" + type.name() + "|";
        return clients.keySet().stream()
                .filter(k -> k.startsWith(prefix))
                .map(k -> k.substring(prefix.length()))
                .sorted()
                .collect(Collectors.toList());
    }

    private String key(RegionName region, ClientType type, String instance) {
        return region.value() + "|" + type.name() + "|" + instance;
    }
}
