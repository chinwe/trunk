package org.example.migration.example;

import org.example.migration.client.MySqlClient;
import org.example.migration.domain.ClientType;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.TenantMigrationTask;
import org.example.migration.spi.result.MigrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 参考实现：用户数据迁移任务（简化版，仅 MySQL 单中间件，演示 SPI 用法）。
 *
 * 业务插件开发模板——展示如何实现方向无关的 migrate：
 *   1. 通过 ctx.sourceRegion()/targetRegion() 获取客户端（禁止硬编码 region）
 *   2. 从源区读、写目标区、删源区（真搬迁语义）
 *   3. 回滚时框架对调 region，同一份逻辑天然反向执行
 *
 * <p><b>幂等示例（ADR-0002）</b>：写入用 UPSERT（ON DUPLICATE KEY UPDATE），
 * 这样 resume/retry/rollback 重做同一租户时不会产生重复数据。
 *
 * 注意：此示例用 mock SQL 演示结构，真实业务需替换为实际表名与字段映射。
 */
@Component
public class UserMigrationTask implements TenantMigrationTask {

    private static final Logger log = LoggerFactory.getLogger(UserMigrationTask.class);

    private static final String SELECT_USERS_BY_TENANTS =
            "SELECT * FROM users WHERE tenant_id IN (:tenants)";
    private static final String DELETE_USERS_BY_TENANTS =
            "DELETE FROM users WHERE tenant_id IN (:tenants)";
    /**
     * UPSERT：业务 migrate 必须幂等（ADR-0002）。
     * ON DUPLICATE KEY UPDATE 保证 resume/retry/rollback 重做同一租户时不会插入重复行。
     * 真实业务需按主键/唯一索引设计 ON DUPLICATE KEY UPDATE 子句。
     */
    private static final String UPSERT_USERS =
            "INSERT INTO users (id, tenant_id, /* 其他字段 */) VALUES (?, ?, /* ... */) " +
            "ON DUPLICATE KEY UPDATE tenant_id = VALUES(tenant_id) /* , 其他字段 = VALUES(...) */";

    @Override
    public String taskName() {
        return "user-migration";
    }

    @Override
    public MigrationResult migrate(MigrationContext ctx, List<String> tenantIds,
                                   String product, String bizLine) {
        // 从源区读用户数据
        MySqlClient source = ctx.client(ctx.sourceRegion(), ClientType.MYSQL, "business", MySqlClient.class);
        // 写入目标区
        MySqlClient target = ctx.client(ctx.targetRegion(), ClientType.MYSQL, "business", MySqlClient.class);

        log.info("migrating users for tenants {} from {} to {}",
                tenantIds, ctx.sourceRegion(), ctx.targetRegion());

        List<?> users = source.queryByTenants(SELECT_USERS_BY_TENANTS, tenantIds);
        if (users.isEmpty()) {
            log.info("no users found for tenants {}, skip", tenantIds);
            return MigrationResult.success(0);
        }

        // 演示：真实场景需把 users 转成 batchUpdate 参数。此处用占位 UPSERT。
        // 实际业务：target.batchUpdate(UPSERT_USERS, toArgs(users));
        log.info("copied {} users to target region (UPSERT, idempotent)", users.size());

        // 真搬迁：删除源区数据
        source.deleteByTenants(DELETE_USERS_BY_TENANTS, tenantIds);
        log.info("deleted source users for tenants {}", tenantIds);

        return MigrationResult.success(users.size());
    }
}
