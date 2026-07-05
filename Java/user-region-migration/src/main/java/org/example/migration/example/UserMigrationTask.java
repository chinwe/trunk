package org.example.migration.example;

import org.example.migration.client.MySqlClient;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.MigrationPhase;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.TenantMigrationTask;
import org.example.migration.spi.result.MigrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 参考实现：用户数据迁移任务（两阶段版，ADR-0005）。
 *
 * 演示如何按 {@link MigrationPhase} 分流迁移：
 * <ul>
 *   <li>CORE：用户基本信息表（users）。登录后立即必需，CORE 阶段完成后即切流。</li>
 *   <li>SECONDARY：用户行为日志表（user_activity）。短时缺失可容忍，切流后在窗口期内搬运。</li>
 * </ul>
 *
 * <p><b>幂等示例（ADR-0002）</b>：写入用 UPSERT（ON DUPLICATE KEY UPDATE），
 * 这样 resume/retry/rollback 重做同一租户时不会产生重复数据。
 *
 * <p><b>SECONDARY merge 示例（ADR-0005）</b>：SECONDARY 在 CORE 切流后执行，用户已在目标区
 * 并可能写入新的次核心数据。SECONDARY 用 {@code updated_at} 时间戳比较 merge——只有源区数据
 * 比目标区新才覆盖，<b>不得简单覆盖</b>目标区已有数据，否则会丢失用户在窗口期内新写入的数据。
 *
 * 注意：此示例用 mock SQL 演示结构，真实业务需替换为实际表名与字段映射。
 */
@Component
public class UserMigrationTask implements TenantMigrationTask {

    private static final Logger log = LoggerFactory.getLogger(UserMigrationTask.class);

    // ── CORE 阶段：用户基本信息 ──
    private static final String SELECT_USERS_BY_TENANTS =
            "SELECT * FROM users WHERE tenant_id IN (:tenants)";
    private static final String DELETE_USERS_BY_TENANTS =
            "DELETE FROM users WHERE tenant_id IN (:tenants)";
    /** UPSERT（ADR-0002 幂等） */
    private static final String UPSERT_USERS =
            "INSERT INTO users (id, tenant_id, /* 其他字段 */) VALUES (?, ?, /* ... */) " +
            "ON DUPLICATE KEY UPDATE tenant_id = VALUES(tenant_id) /* , 其他字段 = VALUES(...) */";

    // ── SECONDARY 阶段：用户行为日志 ──
    private static final String SELECT_USER_ACTIVITY_BY_TENANTS =
            "SELECT * FROM user_activity WHERE tenant_id IN (:tenants)";
    private static final String DELETE_USER_ACTIVITY_BY_TENANTS =
            "DELETE FROM user_activity WHERE tenant_id IN (:tenants)";
    /**
     * SECONDARY 的 UPSERT + 时间戳 merge（ADR-0005）。
     * 仅当源行 updated_at > 目标行 updated_at 时才覆盖——窗口期内用户在目标区新写入的行不会被旧数据覆盖。
     * 真实业务需替换为实际字段；这里用占位 IF 条件演示 merge 原则，不演示完美 merge。
     */
    private static final String UPSERT_USER_ACTIVITY_WITH_MERGE =
            "INSERT INTO user_activity (id, tenant_id, updated_at, /* 其他字段 */) VALUES (?, ?, ?, /* ... */) " +
            "ON DUPLICATE KEY UPDATE updated_at = IF(VALUES(updated_at) > user_activity.updated_at, " +
            "VALUES(updated_at), user_activity.updated_at) " +
            "/* , 其他字段按同款 IF(VALUES(t) > t, VALUES(x), x) merge */";

    @Override
    public String taskName() {
        return "user-migration";
    }

    @Override
    public MigrationResult migrate(MigrationContext ctx, List<String> tenantIds,
                                   String product, String bizLine, MigrationPhase phase) {
        // phase 由框架驱动（ADR-0005）。rollback 时框架也透传 CORE（rollback 只搬核心数据，接缝 2）。
        MySqlClient source = ctx.client(ctx.sourceRegion(), ClientType.MYSQL, "business", MySqlClient.class);
        MySqlClient target = ctx.client(ctx.targetRegion(), ClientType.MYSQL, "business", MySqlClient.class);

        log.info("migrating {} data for tenants {} from {} to {}",
                phase, tenantIds, ctx.sourceRegion(), ctx.targetRegion());

        if (phase == MigrationPhase.CORE) {
            return migrateCore(source, target, tenantIds);
        }
        return migrateSecondary(source, target, tenantIds);
    }

    /** CORE：搬用户基本信息。无写冲突顾虑（CORE 在切流之前，目标区无用户写入）。 */
    private MigrationResult migrateCore(MySqlClient source, MySqlClient target, List<String> tenantIds) {
        List<?> users = source.queryByTenants(SELECT_USERS_BY_TENANTS, tenantIds);
        if (users.isEmpty()) {
            log.info("no users found for tenants {}, skip CORE", tenantIds);
            return MigrationResult.success(0);
        }
        // 演示占位：真实场景用 target.batchUpdate(UPSERT_USERS, toArgs(users))
        log.info("copied {} users to target region (UPSERT, idempotent)", users.size());

        // 真搬迁：删除源区数据
        source.deleteByTenants(DELETE_USERS_BY_TENANTS, tenantIds);
        log.info("deleted source users for tenants {}", tenantIds);

        return MigrationResult.success(users.size());
    }

    /**
     * SECONDARY：搬用户行为日志。窗口期内目标区可能有用户新写入的行——必须用时间戳 merge，
     * 不得简单覆盖（ADR-0005）。框架不假设源区冻结，业务在 migrate 内部自管 merge。
     */
    private MigrationResult migrateSecondary(MySqlClient source, MySqlClient target, List<String> tenantIds) {
        List<?> activities = source.queryByTenants(SELECT_USER_ACTIVITY_BY_TENANTS, tenantIds);
        if (activities.isEmpty()) {
            log.info("no user_activity found for tenants {}, skip SECONDARY", tenantIds);
            return MigrationResult.success(0);
        }
        // 演示占位：真实场景用 target.batchUpdate(UPSERT_USER_ACTIVITY_WITH_MERGE, toArgs(activities))
        log.info("copied {} user_activity rows to target region (UPSERT with updated_at merge)", activities.size());

        // 真搬迁：删除源区数据
        source.deleteByTenants(DELETE_USER_ACTIVITY_BY_TENANTS, tenantIds);
        log.info("deleted source user_activity for tenants {}", tenantIds);

        return MigrationResult.success(activities.size());
    }
}
