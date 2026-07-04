package org.example.migration.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 框架自动装配：启用 RegionProperties 与 MigrationProperties 的配置绑定。
 */
@Configuration
@EnableConfigurationProperties({ RegionProperties.class, MigrationProperties.class })
public class MigrationAutoConfiguration {
}
