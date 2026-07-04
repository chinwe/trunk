# user-region-migration

> 用户区域迁移框架。一个**框架**，非端到端业务实现——提供多数据源通用迁移能力、可靠性、回滚机制、迁移状态维护，各业务组件在本框架内自行开发核心数据迁移逻辑。
>
> 目标场景：SaaS 服务将新加坡大区的缅甸国家独立迁移到缅甸大区。框架长期保留，后续其他区域拆分复用。

设计详见 [docs/design-spec.md](docs/design-spec.md)。

---

## 技术栈

- Spring Boot 3.2.10 + JDK 21 + Maven
- Spring Shell 3.2.8（CLI 命令框架）
- Jasypt 3.0.5（配置加密）
- 测试栈：JUnit 5 + AssertJ + Mockito

---

## 快速开始

### 构建

```bash
mvn clean package
```

### 交互式运行（生产）

```bash
java -jar target/user-region-migration-1.0-SNAPSHOT.jar --spring.shell.interactive.enabled=true --spring.profiles.active=prod
```

进入 Shell 后可执行命令（`tasks` 查看已注册业务插件）。

### 命令一览

```
migrate    --task <name> --source <region> --target <region>
           --product <p> --biz-line <b>
           [--tenants <t1,t2,...>] [--batch-size 50] [--threads 4]
  正向迁移：扫租户(或手动指定)→搬数据→总量闸门→切流

rollback   --run-id <id> --task <name>
  逆向回滚（方向对调复用 migrate）

resume     --run-id <id> --task <name>
  从断点续传（只处理 PENDING 租户）

verify     --run-id <id> --task <name>
  对账（调用业务插件 verify 钩子）

status     --run-id <id>
  查询迁移状态

tasks
  列出已注册业务插件

dry-run    --source <region> --target <region> [--tenants <t1,t2,...>]
  预估待迁移租户数（不写数据）
```

`--tenants` 手动指定租户；不填则通过 TenantScanner 自动扫描源区。

---

## 业务插件开发指南

业务组件通过实现两个 SPI 接口接入框架。

### 1. 实现迁移任务

```java
@Component
public class UserMigrationTask implements TenantMigrationTask {

    @Override
    public String taskName() {
        return "user-migration";  // 命令通过 --task 引用
    }

    @Override
    public MigrationResult migrate(MigrationContext ctx, List<String> tenantIds,
                                   String product, String bizLine) {
        // 【关键】必须方向无关：通过 ctx.sourceRegion()/targetRegion() 获取客户端，
        //        禁止硬编码具体 region。这样回滚时框架对调 region，同一份逻辑反向执行。
        MySqlClient source = ctx.client(ctx.sourceRegion(), ClientType.MYSQL, MySqlClient.class);
        MySqlClient target = ctx.client(ctx.targetRegion(), ClientType.MYSQL, MySqlClient.class);

        // 从源区读 → 写目标区 → 删源区（真搬迁）
        List<?> users = source.queryByTenants("SELECT * FROM users WHERE tenant_id IN ...", tenantIds);
        // target.batchUpdate("INSERT INTO users ...", toArgs(users));
        source.deleteByTenants("DELETE FROM users WHERE tenant_id IN ...", tenantIds);

        return MigrationResult.success(users.size());
    }

    // 可选：深度对账
    @Override
    public VerifyResult verify(MigrationContext ctx, List<String> tenantIds,
                               String product, String bizLine) {
        // 实现总量/抽样/逐条校验
        return VerifyResult.passed(tenantIds.size());
    }
}
```

### 2. 实现切流动作（可选）

```java
@Component("user-migration")  // bean 名必须与 taskName 对齐
public class UserCutoverAction implements CutoverAction {

    @Override
    public void evict(MigrationContext ctx, List<String> tenantIds,
                      String product, String bizLine) {
        // 踢登录：删目标区 Redis 会话，用户重登重定向到新区域
        RedisClient redis = ctx.client(ctx.targetRegion(), ClientType.REDIS, RedisClient.class);
        // ... 删 token
    }
}
```

### 3. 方向无关原则（必读）

这是框架的核心设计。业务**只实现一个 migrate 方法**，正向迁移与逆向回滚复用同一份逻辑：

| | sourceRegion() | targetRegion() |
|---|---|---|
| 正向 migrate 命令 | 源区（如新加坡） | 目标区（如缅甸） |
| rollback 命令 | 原目标区（缅甸） | 原源区（新加坡） |

业务 migrate 内部"从 source 读 → 写 target → 删 source"的逻辑，在回滚时天然变成"从缅甸读 → 写新加坡 → 删缅甸"。**不要写两个方向的方法。**

### 4. 单租户内跨中间件的补偿回滚

框架不碰单租户内部事务（跨中间件无分布式事务）。业务在 migrate 内自管补偿：

```java
public MigrationResult migrate(MigrationContext ctx, List<String> tenantIds, ...) {
    try {
        writeMysql(...);
        writeDynamoDb(...);
        writeS3(...);
    } catch (Exception e) {
        // 对已写成功的中间件逐个回删（补偿）
        rollbackMysql(...);
        rollbackDynamoDb(...);
        throw e;  // 框架捕获后标记租户 FAILED
    }
}
```

框架捕获异常 → 标记租户 FAILED + 记录 errorContext → 整批其余租户继续（单租户隔离）。

---

## 进阶功能

### 多实例（MySQL/Redis）

MySQL 和 Redis 支持在同一个 region 下注册多个命名实例（如 MySQL 的 `business`、`open` 库，Redis 的 `session`、`cache`）。业务通过四参 API 指定实例名：

```java
// 指定 MySQL 的 "business" 实例
MySqlClient source = ctx.client(ctx.sourceRegion(), ClientType.MYSQL, "business", MySqlClient.class);
// 指定 Redis 的 "session" 实例
RedisClient redis = ctx.client(ctx.targetRegion(), ClientType.REDIS, "session", RedisClient.class);
```

ES/S3/DynamoDB/Kafka 等单实例中间件使用三参 API（内部自动填充 instance="default"）。

### 租户扫描（TenantScanner SPI）

迁移命令的 `--tenants` 为空时，框架通过 `TenantScanner` 从源区自动扫描待迁移租户：

```java
// 框架内置 MySQL 实现（默认查询 tenant 表）
// 自定义实现：实现 TenantScanner 接口并声明为 Spring Bean
@Component
public class CustomTenantScanner implements TenantScanner {
    @Override
    public List<String> scanSourceTenants(MigrationContext ctx) {
        // 从源区自定逻辑读取租户ID列表
    }
}
```

### 对账计数器（ReconciliationCounter SPI）

`migrate` 命令的切流前总量闸门依赖 `ReconciliationCounter` 做 COUNT 级校验。框架不提供默认实现——业务需实现并注册为 Spring Bean：

```java
@Component
public class UserReconciliationCounter implements ReconciliationCounter {
    @Override
    public long count(RegionName region, MigrationRun run) {
        // 统计指定 region 在本次迁移范围内的记录数（如 MySQL COUNT）
    }
}
```

- 已注册 → `CountReconciliationGate` 校验源/目标计数一致才切流
- 未注册 → `AlwaysPassReconciliationGate` 默认通过（不阻塞）

### 令牌桶限流

框架内置 `TokenBucketRateLimiter`（CAS 实现），各中间件按 `migration.rate-limit.<type>.qps` 配置分别限流，防止压垮源/目标中间件。

### 通知器（MigrationNotifier）

切流成功后框架通过 `MigrationNotifier` 向源区/目标区发送 Kafka 通知（默认实现 `KafkaMigrationNotifier`）。业务可实现 `MigrationNotifier` 接口并声明为 Spring Bean 覆盖默认行为。

---

## 测试

```bash
mvn test
```

测试覆盖：
- **引擎核心行为**（TDD）：单租户隔离、租户级断点续传、切流总量闸门、全流程编排、回滚方向无关
- **状态层契约**：CheckpointStore 的 run/租户状态管理
- **配置绑定**：RegionProperties 多 region 多中间件结构
- **客户端注册表**：按 (region, type) 查表

测试用 fake SPI + 内存 store，不依赖真实中间件，秒级完成。

---

## 包结构

```
org.example.migration
├── spi/          业务插件接口（TenantMigrationTask / CutoverAction / MigrationContext）
│   └── result/   结果类型（MigrationResult / VerifyResult）
├── client/       多中间件客户端抽象（RegionClient + 6 子接口 + Registry + 6 实现）
├── engine/       框架内核（MigrationEngine / CheckpointStore / ReconciliationGate /
│                 TenantScanner / ReconciliationCounter / TokenBucketRateLimiter /
│                 MigrationNotifier / MigrationRequest）
├── config/       配置绑定与自动装配（RegionProperties / MigrationProperties /
│                 MigrationAutoConfiguration / RegionClientAutoConfiguration /
│                 MigrationInfrastructureConfiguration）
├── shell/        Spring Shell 命令（MigrationCommands / ShellApplication / TaskRegistry）
├── domain/       领域模型（RegionName / ClientType / Direction / RunStatus / TenantStatus）
│   └── entity/   实体（MigrationRun / MigrationTenantState）
└── example/      参考实现（UserMigrationTask / UserCutoverAction）
```

---

## 配置

region 连接信息通过 `application-{profile}.yml` 配置，敏感字段用 Jasypt `ENC(...)` 加密：

```yaml
regions:
  singapore:
    mysql:
      business: { jdbc-url: ENC(...), username: ENC(...), password: ENC(...) }
    redis: { host: ENC(...), port: 6379, password: ENC(...) }
    ...
```

主密钥通过环境变量 `JASYPT_MASTER_PASSWORD` 注入。
