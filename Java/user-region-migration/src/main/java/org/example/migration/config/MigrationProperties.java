package org.example.migration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 迁移框架运行参数：分批大小、并发线程数、各中间件限流、重试策略。
 * 对应 application.yml 的 migration.* 结构。
 */
@Data
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    /** 默认租户分批大小（每批租户数） */
    private int defaultBatchSize = 50;
    /** 默认并发线程数（批次间并发） */
    private int defaultThreads = 4;
    /** 各中间件限流配置 */
    private Map<String, RateLimitConfig> rateLimit;
    /** 重试策略 */
    private RetryConfig retry;

    /** 单中间件限流配置 */
    @Data
    public static class RateLimitConfig {
        private int qps;
    }

    /** 重试策略配置 */
    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private String backoffInitial = "1s";
    }
}
