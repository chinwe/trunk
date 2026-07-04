package org.example.migration.engine;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 令牌桶限流器。按补充速率（令牌/秒）补充令牌，容量上限固定。
 *
 * 用于控制迁移对源/目标中间件的访问速率，防止压垮生产服务。
 * 线程安全：基于时间戳的乐观计算 + 同步获取。
 */
public class TokenBucketRateLimiter {

    private final int capacity;
    private final double refillTokensPerSecond;

    // 持有当前令牌数与上次补充时间戳
    private final AtomicReference<State> state;

    public TokenBucketRateLimiter(int capacity, int refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.state = new AtomicReference<>(new State(capacity, System.nanoTime()));
    }

    /**
     * 尝试获取 n 个令牌。成功返回 true 并扣减；失败返回 false 不扣减。
     */
    public boolean tryAcquire(int n) {
        while (true) {
            State current = state.get();
            double nowTokens = refill(current);
            if (nowTokens < n) {
                return false;
            }
            State next = new State(nowTokens - n, System.nanoTime());
            if (state.compareAndSet(current, next)) {
                return true;
            }
            // CAS 失败则重试
        }
    }

    /** 根据时间差补充令牌（不超过容量） */
    private double refill(State s) {
        long now = System.nanoTime();
        long elapsedNanos = now - s.lastRefillNanos;
        if (elapsedNanos <= 0) {
            return s.tokens;
        }
        double added = elapsedNanos / 1_000_000_000.0 * refillTokensPerSecond;
        return Math.min(capacity, s.tokens + added);
    }

    private record State(double tokens, long lastRefillNanos) {
    }
}
