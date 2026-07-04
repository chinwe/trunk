package org.example.migration.example;

import org.example.migration.client.KafkaClient;
import org.example.migration.client.RedisClient;
import org.example.migration.domain.ClientType;
import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.MigrationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 参考实现：用户切流动作（mock 演示）。
 *
 * 踢登录示例：删除目标区 Redis 中这些租户的会话 token，用户重登时重定向到新区域。
 * Kafka 通知：发送迁入通知，业务收到后清理内存数据。
 *
 * bean 名 "user-migration" 与 taskName 对齐，使 ShellAutoConfiguration 能关联。
 * 真实业务的会话机制（Redis token 结构 / 鉴权接口）需按实际调整。
 */
@Component("user-migration")
public class UserCutoverAction implements CutoverAction {

    private static final Logger log = LoggerFactory.getLogger(UserCutoverAction.class);

    @Override
    public void evict(MigrationContext ctx, List<String> tenantIds, String product, String bizLine) {
        log.info("cutover: evicting sessions for tenants {} in target region {}", tenantIds, ctx.targetRegion());

        // 踢登录：删除目标区 Redis 会话（示例：按租户扫描 session key）
        RedisClient targetRedis = ctx.client(ctx.targetRegion(), ClientType.REDIS, RedisClient.class);
        List<String> sessionKeys = targetRedis.scanKeysByTenants("session:*:", tenantIds);
        for (String key : sessionKeys) {
            targetRedis.delete(key);
        }
        log.info("evicted {} sessions", sessionKeys.size());

        // 发送迁入通知（示例）
        KafkaClient targetKafka = ctx.client(ctx.targetRegion(), ClientType.KAFKA, KafkaClient.class);
        targetKafka.send("migration-notify", "tenants migrated in: " + tenantIds);
    }
}
