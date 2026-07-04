package org.example.migration.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

/**
 * 重试策略——包装 Spring Retry 的 RetryTemplate，提供编程式重试。
 *
 * 仅对瞬时性异常（RuntimeException 子类）重试，最多 maxAttempts 次，
 * 指数退避（初始间隔 * 2^(attempt-1)）。
 *
 * 这是 {@link MigrationEngine} 的内部模块（package-private），
 * 遵循"两个适配器 → 真实接缝"原则，当前只有一个 RetryTemplate 适配器。
 */
class RetryStrategy {

    private static final Logger log = LoggerFactory.getLogger(RetryStrategy.class);

    private final RetryTemplate retryTemplate;

    /**
     * @param maxAttempts     最大尝试次数（含首次）
     * @param backoffInitial  初始退避间隔（毫秒）
     */
    RetryStrategy(int maxAttempts, long backoffInitial) {
        this.retryTemplate = buildTemplate(maxAttempts, backoffInitial);
    }

    /**
     * 执行带重试的迁移操作。
     *
     * @param tenantId 租户ID（仅用于日志上下文）
     * @param action   待重试的操作
     * @throws RuntimeException 所有重试耗尽后抛出最后一次异常
     */
    void executeWithRetry(String tenantId, Runnable action) {
        try {
            retryTemplate.execute(ctx -> {
                action.run();
                return null;
            });
        } catch (RuntimeException e) {
            log.debug("tenant {} exhausted all retries: {}", tenantId, e.getMessage());
            throw e;
        }
    }

    /** 无重试策略：仅执行一次，失败即抛出。用于未配置 retry 的场景 */
    static RetryStrategy noRetry() {
        return new RetryStrategy(1, 0);
    }

    private static RetryTemplate buildTemplate(int maxAttempts, long backoffInitial) {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                maxAttempts,
                Map.of(RuntimeException.class, true), // 所有 RuntimeException 可重试
                true // 可遍历的异常链也匹配
        );
        template.setRetryPolicy(retryPolicy);

        if (backoffInitial > 0) {
            ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
            backOff.setInitialInterval(backoffInitial);
            backOff.setMultiplier(2.0);
            backOff.setMaxInterval(10_000L); // 最大 10 秒
            template.setBackOffPolicy(backOff);
        }

        return template;
    }
}
