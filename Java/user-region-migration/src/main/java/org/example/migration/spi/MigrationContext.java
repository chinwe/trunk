package org.example.migration.spi;

import org.example.migration.client.RegionClient;
import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;

/**
 * 迁移上下文。框架在调用业务插件时注入，业务插件通过它访问 region 客户端与运行配置。
 *
 * 契约：业务插件的 migrate 实现必须基于 sourceRegion()/targetRegion() 获取客户端，
 * 禁止硬编码具体 region。这是"方向无关"原则的保证——回滚时框架对调 source/target，
 * 同一个 migrate 实现自然反向执行。
 */
public interface MigrationContext {

    /** 源区域（正向=迁出区，回滚=原目标区） */
    RegionName sourceRegion();

    /** 目标区域（正向=迁入区，回滚=原源区） */
    RegionName targetRegion();

    /**
     * 获取指定 region 的中间件客户端。
     *
     * @param region 区域
     * @param type   中间件类型
     * @param clazz  期望的子接口类型
     * @return 客户端实例
     */
    <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz);

    /** 迁移运行参数（分批大小、线程数、限流等） */
    MigrationProperties config();
}
