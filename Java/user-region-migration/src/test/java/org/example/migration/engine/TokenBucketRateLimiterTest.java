package org.example.migration.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenBucketRateLimiter 测试：验证令牌桶限流行为。
 */
class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("桶满时立即放行,消耗令牌")
    void shouldAcquireWhenTokensAvailable() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 10);

        assertThat(limiter.tryAcquire(5)).isTrue();
        assertThat(limiter.tryAcquire(5)).isTrue();
    }

    @Test
    @DisplayName("令牌不足时拒绝")
    void shouldRejectWhenInsufficientTokens() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 10);

        assertThat(limiter.tryAcquire(10)).isTrue();
        assertThat(limiter.tryAcquire(1)).isFalse();
    }

    @Test
    @DisplayName("等待补充后可再次获取(补充速率 refillTokensPerSecond)")
    void shouldRefillOverTime() throws InterruptedException {
        // 1 令牌/秒,初始 1 令牌
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1);
        assertThat(limiter.tryAcquire(1)).isTrue();

        // 等待令牌补充
        Thread.sleep(1100);
        assertThat(limiter.tryAcquire(1)).isTrue();
    }

    @Test
    @DisplayName("补充不超过桶容量")
    void shouldNotExceedCapacity() throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 10);
        // 等待足够时间补充,但不应超过容量 2
        Thread.sleep(500);
        assertThat(limiter.tryAcquire(2)).isTrue();
        assertThat(limiter.tryAcquire(1)).isFalse();
    }
}
