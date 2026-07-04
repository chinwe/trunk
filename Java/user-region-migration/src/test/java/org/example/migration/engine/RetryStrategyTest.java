package org.example.migration.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RetryStrategy 测试：验证 Spring Retry RetryTemplate 的正确集成。
 */
class RetryStrategyTest {

    @Test
    @DisplayName("首次成功时不重试")
    void shouldNotRetryOnSuccess() {
        RetryStrategy strategy = new RetryStrategy(3, 10);
        AtomicInteger calls = new AtomicInteger();
        strategy.executeWithRetry("t1", calls::incrementAndGet);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("瞬时失败自动重试,最终成功")
    void shouldRetryOnTransientFailure() {
        RetryStrategy strategy = new RetryStrategy(3, 10);
        AtomicInteger calls = new AtomicInteger();
        strategy.executeWithRetry("t1", () -> {
            if (calls.incrementAndGet() < 2) {
                throw new RuntimeException("transient error");
            }
            // 第三次成功
        });
        // 第一次失败,第二次成功 — 共 2 次调用
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("所有重试耗尽后抛出最后一次异常")
    void shouldThrowAfterExhaustingRetries() {
        RetryStrategy strategy = new RetryStrategy(2, 10);
        assertThatThrownBy(() -> strategy.executeWithRetry("t1", () -> {
            throw new RuntimeException("persistent error");
        })).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("persistent error");
    }

    @Test
    @DisplayName("noRetry 策略仅执行一次,失败即抛出")
    void noRetryExecutesOnce() {
        RetryStrategy strategy = RetryStrategy.noRetry();
        AtomicInteger calls = new AtomicInteger();
        assertThatThrownBy(() -> strategy.executeWithRetry("t1", () -> {
            calls.incrementAndGet();
            throw new RuntimeException("fail");
        })).isInstanceOf(RuntimeException.class);
        assertThat(calls.get()).isEqualTo(1);
    }
}
