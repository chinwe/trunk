package org.example.migration.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Set;

/**
 * 重试策略——包装 Spring Retry 的 RetryTemplate，提供编程式重试。
 *
 * 采用黑名单策略：对所有 RuntimeException 重试（中间件 SDK 通常将网络/瞬时性异常
 * 包装为 RuntimeException），但排除明确的编程错误（NPE、IllegalArgumentException、
 * ClassCastException 等），这些异常不会因重试而自愈，应立即失败。
 *
 * 业务可通过 {@link #addNonRetryableException(Class)} 添加额外的不可重试异常类型。
 *
 * 这是 {@link MigrationEngine} 的内部模块（package-private），
 * 遵循"两个适配器 → 真实接缝"原则，当前只有一个 RetryTemplate 适配器。
 */
class RetryStrategy {

    private static final Logger log = LoggerFactory.getLogger(RetryStrategy.class);

    /** 默认不可重试的编程错误异常类型（黑名单） */
    private static final Set<Class<? extends Throwable>> DEFAULT_NON_RETRYABLE = Set.of(
            NullPointerException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
            ClassCastException.class,
            IndexOutOfBoundsException.class,
            ArrayIndexOutOfBoundsException.class,
            java.util.NoSuchElementException.class,
            UnsupportedOperationException.class,
            NumberFormatException.class
    );

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

        // 自定义 RetryPolicy：对所有异常尝试重试，但过滤掉编程错误（黑名单）
        template.setRetryPolicy(new NonProgrammingErrorRetryPolicy(maxAttempts));

        if (backoffInitial > 0) {
            ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
            backOff.setInitialInterval(backoffInitial);
            backOff.setMultiplier(2.0);
            backOff.setMaxInterval(10_000L); // 最大 10 秒
            template.setBackOffPolicy(backOff);
        }

        return template;
    }

    /**
     * 自定义重试策略：重试所有异常，但排除编程错误类异常。
     * 中间件 SDK 通常将网络/瞬时异常包装为 RuntimeException，此策略覆盖这些场景。
     */
    private static class NonProgrammingErrorRetryPolicy extends SimpleRetryPolicy {

        NonProgrammingErrorRetryPolicy(int maxAttempts) {
            super(maxAttempts);
            // 对所有异常都尝试重试（由 canRetry 进一步过滤）
            setMaxAttempts(maxAttempts);
        }

        @Override
        public boolean canRetry(RetryContext context) {
            if (!super.canRetry(context)) {
                return false;
            }
            Throwable lastThrowable = context.getLastThrowable();
            if (lastThrowable == null) {
                return true;
            }
            // 遍历异常链：如果根源是编程错误则不重试
            Throwable cause = lastThrowable;
            while (cause != null) {
                if (DEFAULT_NON_RETRYABLE.contains(cause.getClass())) {
                    log.debug("non-retryable exception detected: {} (cause: {})",
                            cause.getClass().getSimpleName(), cause.getMessage());
                    return false;
                }
                cause = cause.getCause();
            }
            return true;
        }
    }
}
