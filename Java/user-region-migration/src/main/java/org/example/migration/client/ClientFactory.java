package org.example.migration.client;

import org.example.migration.config.RegionProperties;

/**
 * 客户端工厂 SPI。每种中间件提供一个 ClientFactory 实现，
 * 由 {@link org.example.migration.config.RegionClientAutoConfiguration} 自动收集并委托创建客户端。
 *
 * 添加新中间件类型：实现此接口并声明为 Spring Bean 即可，
 * 无需修改 RegionClientAutoConfiguration。
 */
public interface ClientFactory {

    /**
     * 返回本工厂支持的中间件类型。
     */
    org.example.migration.domain.ClientType supportedType();

    /**
     * 创建指定 region 的客户端实例。
     *
     * @param config       该 region 的中间件配置
     * @param instanceName 实例名（多实例中间件如 MySQL/Redis 用，单实例中间件为 "default"）
     * @return 创建的客户端，或 null 表示该 region 未配置此中间件
     */
    RegionClient create(RegionProperties.RegionConfig config, String instanceName);

    /**
     * 本中间件是否支持多实例（MySQL/Redis 为 true，其余为 false）。
     * 多实例中间件的配置是 Map<String, ...> 结构，需要遍历实例名。
     */
    default boolean supportsMultiInstance() {
        return false;
    }
}
