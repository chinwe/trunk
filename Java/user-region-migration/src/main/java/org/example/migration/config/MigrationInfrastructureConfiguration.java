package org.example.migration.config;

import org.example.migration.client.RegionClientRegistry;
import org.example.migration.engine.KafkaMigrationNotifier;
import org.example.migration.engine.MigrationNotifier;
import org.example.migration.engine.TenantScanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 迁移基础设施自动装配：提供 Notifier / Counter / TenantScanner 的默认 Bean。
 *
 * 业务可通过实现同名接口并声明为 Bean 覆盖默认行为。
 * - MigrationNotifier: 默认 Kafka 实现
 * - ReconciliationCounter: 默认 null（gate 默认通过）
 * - TenantScanner: 默认 MySQL 实现（扫描 tenant 表）
 */
@Configuration
public class MigrationInfrastructureConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MigrationNotifier migrationNotifier(RegionClientRegistry registry) {
        return new KafkaMigrationNotifier(registry);
    }

    // ReconciliationCounter 不提供默认 Bean；业务按需实现。命令层用 ObjectProvider 容忍缺失。

    @Bean
    @ConditionalOnMissingBean
    public TenantScanner tenantScanner() {
        // 默认扫描名为 "tenant" 的表
        return new TenantScanner.MySqlTenantScanner("tenant");
    }
}
