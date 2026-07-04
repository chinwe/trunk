package org.example.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 迁移框架运行参数：分批大小、并发线程数、各中间件限流、重试策略。
 * 对应 application.yml 的 migration.* 结构。
 */
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

    public int getDefaultBatchSize() { return defaultBatchSize; }
    public void setDefaultBatchSize(int defaultBatchSize) { this.defaultBatchSize = defaultBatchSize; }
    public int getDefaultThreads() { return defaultThreads; }
    public void setDefaultThreads(int defaultThreads) { this.defaultThreads = defaultThreads; }
    public Map<String, RateLimitConfig> getRateLimit() { return rateLimit; }
    public void setRateLimit(Map<String, RateLimitConfig> rateLimit) { this.rateLimit = rateLimit; }
    public RetryConfig getRetry() { return retry; }
    public void setRetry(RetryConfig retry) { this.retry = retry; }

    /** 单中间件限流配置 */
    public static class RateLimitConfig {
        private int qps;

        public int getQps() { return qps; }
        public void setQps(int qps) { this.qps = qps; }
    }

    /** 重试策略配置 */
    public static class RetryConfig {
        private int maxAttempts = 3;
        private String backoffInitial = "1s";

        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public String getBackoffInitial() { return backoffInitial; }
        public void setBackoffInitial(String backoffInitial) { this.backoffInitial = backoffInitial; }
    }
}
