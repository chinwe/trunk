package org.example.migration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 迁移框架运行参数：分批大小、并发线程数、限流、重试策略。
 * 对应 application.yml 的 migration.* 结构。
 */
@Data
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    /** 进度汇报粒度（每完成 N 个租户打一次日志）。注意：不再驱动并发，见 ADR-0003 */
    private int defaultBatchSize = 50;
    /** 租户级并发线程数 */
    private int defaultThreads = 4;
    /** 单租户迁移超时（分钟），0 表示不设超时 */
    private long tenantTimeoutMinutes = 30;
    /** 进程级单一令牌桶的全局 QPS，**按批 acquire**（每批 1 个令牌）。默认 500，设 0 表示不限流。
     *  语义：每秒最多启动 N 个批（限调度速率，不限批内中间件访问速率——后者由业务自管）。 */
    private int rateLimitQps = 500;
    /** 重试策略 */
    private RetryConfig retry;

    /** 重试策略配置 */
    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private String backoffInitial = "1s";
    }
}
