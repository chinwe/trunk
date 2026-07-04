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

    @Test
    @DisplayName("阻塞获取 acquire 在令牌不足时等待后成功")
    void shouldBlockUntilTokensAvailable() {
        // 1 令牌,高补充速率,立即消耗后阻塞等待补充
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 100); // 100/s 补充
        assertThat(limiter.tryAcquire(1)).isTrue();
        // acquire 阻塞等待直到 token 补充
        limiter.acquire(1);
        // 成功获取(不抛异常即通过)
    }

    @Test
    @DisplayName("noop 限流器总是立刻通过,不阻塞不消耗")
    void noopLimiterAlwaysPasses() {
        TokenBucketRateLimiter limiter = TokenBucketRateLimiter.noop();
        for (int i = 0; i < 1000; i++) {
            assertThat(limiter.tryAcquire(1000)).isTrue();
        }
        // acquire 也不阻塞
        limiter.acquire(1);
    }
}
