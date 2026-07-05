package org.example.migration.config;

import org.example.migration.client.RegionClientRegistry;
import org.example.migration.engine.KafkaMigrationNotifier;
import org.example.migration.engine.MigrationNotifier;
import org.example.migration.engine.MySqlTenantScanner;
import org.example.migration.engine.TenantScanner;
import org.example.migration.engine.TokenBucketRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 迁移基础设施自动装配：提供 Notifier / RateLimiter / TenantScanner 的默认 Bean。
 *
 * 业务可通过实现同名接口并声明为 Bean 覆盖默认行为。
 * - MigrationNotifier: 默认 Kafka 实现
 * - ReconciliationChecker: 不提供默认 Bean；业务按需实现。命令层用 ObjectProvider 容忍缺失。
 * - TokenBucketRateLimiter: 进程级单例（R3），保证多 run 并发时全局 QPS 真正受控
 * - TenantScanner: 默认 MySQL 实现（扫描 tenant 表）
 */
@Configuration
public class MigrationInfrastructureConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MigrationNotifier migrationNotifier(RegionClientRegistry registry) {
        return new KafkaMigrationNotifier(registry);
    }

    /**
     * 进程级单例令牌桶（R3）：保证同时跑多个 run 时全局 QPS 真正受控，
     * 而非每个 run 各自一个令牌桶导致实际 QPS 翻倍。
     */
    @Bean
    @ConditionalOnMissingBean
    public TokenBucketRateLimiter migrationRateLimiter(MigrationProperties props) {
        int qps = props.getRateLimitQps();
        if (qps <= 0) {
            return TokenBucketRateLimiter.noop();
        }
        return new TokenBucketRateLimiter(qps, qps);
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantScanner tenantScanner() {
        // 默认扫描名为 "tenant" 的表
        return new MySqlTenantScanner("tenant");
    }
}
