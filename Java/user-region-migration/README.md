# user-region-migration

> 用户区域迁移框架。一个**框架**，非端到端业务实现——提供多数据源通用迁移能力、可靠性、回滚机制、迁移状态维护，各业务组件在本框架内自行开发核心数据迁移逻辑。
>
> 目标场景：SaaS 服务将新加坡大区的缅甸国家独立迁移到缅甸大区。框架长期保留，后续其他区域拆分复用。

设计详见 [docs/design-spec.md](docs/design-spec.md)。

---

## 技术栈

- Spring Boot 3.2.12 + JDK 21 + Maven
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
migrate    --task <name> -s <region> --target <region>
           [--product <p>] [--biz-line <b>]
           [--tenants <t1,t2,...>] [--batch-size 50] [--threads 4]
  正向迁移（两阶段）：CORE→切流→SECONDARY（ADR-0005）
  短选项：-t <name> = --task, -s <region> = --source

rollback   --run-id <id> -t <name>
  逆向回滚：方向对调复用 migrate（已切流态——CORE_CUTOVER_DONE/RUNNING_SECONDARY/DONE——拒绝执行）

resume     --run-id <id> -t <name>
  从断点续传：按 Run 状态分发（CORE/SECONDARY）

verify     --run-id <id> -t <name>
  对账（调用业务插件 verify 钩子）

status     --run-id <id>
  查询迁移状态（含两阶段标识：status/phase 字段）

tasks
  列出已注册业务插件

dry-run    -s <region> --target <region> [--tenants <t1,t2,...>]
  预估待迁移租户数与两阶段编排（不写数据）
```

`--tenants` 手动指定租户；不填则通过 TenantScanner 自动扫描源区。

---

## 业务插件开发指南

业务组件通过实现两个 SPI 接口接入框架。

### 1. 实现迁移任务

业务实现 `TenantMigrationTask`，按 `MigrationPhase` 分流（ADR-0005）：

```java
@Component
public class UserMigrationTask implements TenantMigrationTask {

    @Override
    public String taskName() {
        return "user-migration";  // 命令通过 --task 引用
    }

    @Override
    public MigrationResult migrate(MigrationContext ctx, List<String> tenantIds,
                                   String product, String bizLine, MigrationPhase phase) {
        // 【关键】必须方向无关：通过 ctx.sourceRegion()/targetRegion() 获取客户端，
        //        禁止硬编码具体 region。这样回滚时框架对调 region，同一份逻辑反向执行。
        MySqlClient source = ctx.client(ctx.sourceRegion(), ClientType.MYSQL, "business", MySqlClient.class);
        MySqlClient target = ctx.client(ctx.targetRegion(), ClientType.MYSQL, "business", MySqlClient.class);

        if (phase == MigrationPhase.CORE) {
            // 核心数据：登录后立即必需（基本信息/订单等）。切流前执行，目标区无用户写入。
            List<?> users = source.queryByTenants("SELECT * FROM users WHERE tenant_id IN ...", tenantIds);
            // 【幂等契约 ADR-0002】写入必须幂等：用 UPSERT
            // target.batchUpdate("INSERT INTO users (...) VALUES (...) ON DUPLICATE KEY UPDATE ...", toArgs(users));
            source.deleteByTenants("DELETE FROM users WHERE tenant_id IN ...", tenantIds);
            return MigrationResult.success(users.size());
        }

        // SECONDARY 阶段：次核心数据（行为日志等）。切流后执行，用户已在目标区写入，
        //                必须用 UPSERT + updated_at merge，不得简单覆盖（ADR-0005）。
        List<?> activities = source.queryByTenants("SELECT * FROM user_activity WHERE tenant_id IN ...", tenantIds);
        // target.batchUpdate("INSERT INTO user_activity (...) ON DUPLICATE KEY UPDATE updated_at = IF(...)", ...);
        source.deleteByTenants("DELETE FROM user_activity WHERE tenant_id IN ...", tenantIds);
        return MigrationResult.success(activities.size());
    }

    // 可选：深度对账
    @Override
    public VerifyResult verify(MigrationContext ctx, List<String> tenantIds,
                               String product, String bizLine) {
        return VerifyResult.passed(tenantIds.size());
    }
}
```

### 2. 实现切流动作（可选）

**幂等契约（ADR-0005）**：evict 必须幂等——删 Redis token（天然幂等）或调"删除会话"语义鉴权接口。不得用"触发推送通知"等非幂等语义。

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

### 4. 幂等契约（必读）

业务 `migrate` **必须幂等**——同一租户被调用 N 次与 1 次结果等价（ADR-0002）。这是框架"可恢复/可逆"承诺的数学前提：

- `resume` 会重做 PENDING 与卡在 RUNNING 的孤儿租户（崩溃在中间态的租户，依赖幂等才能安全重做）；
- `RetryStrategy` 在瞬时失败后重试同一租户；
- `rollback` 对原 DONE 租户反向再调一次 migrate。

建议用 `INSERT ... ON DUPLICATE KEY UPDATE` / `INSERT IGNORE` / 先删后插；S3 等按键寻址的中间件天然幂等。**非幂等的 INSERT 会在第一次 resume/retry 时产生重复数据。**

### 5. 单租户内跨中间件的补偿回滚

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

框架捕获异常 → 整批所有租户标 FAILED + 记录 errorContext → 其余批继续（批级隔离，ADR-0004）。

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

### 对账校验器（ReconciliationChecker SPI）

`migrate` 命令切流前调用 `ReconciliationChecker` 做一致性校验。**算法由业务自证**（ADR-0001）——业务可用 count 差、checksum、抽样或任意方式证明源/目标一致，框架只接受二值判定。框架不提供默认实现——业务需实现并注册为 Spring Bean：

```java
@Component
public class UserReconciliationChecker implements ReconciliationChecker {
    @Override
    public boolean consistent(MigrationRun run, List<String> migratedTenantIds, MigrationPhase phase) {
        // 业务自证：按 phase 分流对账数据集。CORE 对账核心表，SECONDARY 对账次核心表。
        // 可用 count 差、checksum、抽样、逐条比对或组合。
    }
}
```

- 已注册 → `CheckerReconciliationGate` 委托业务自证，通过才切流
- 未注册 → `AlwaysPassReconciliationGate` 默认通过（不阻塞）

> **为什么不是"count 源 vs 目标相等"？** 业务 migrate 是真搬迁（删源）语义，被搬的数据在源区已删，源 count ≈ 0、目标 count = N，"相等"在数学上不成立。业务自证一致性让算法贴合具体数据模型。

> **零失败硬规则**：即使 ReconciliationChecker 通过，只要本次 run 有任意 FAILED 租户，框架也不会切流、Run 标 FAILED（切流不可逆，跳过失败租户切流等于赌"该租户不重要"）。失败租户可被 `resume` 重试（依赖业务 migrate 幂等）。

### 令牌桶限流

框架内置 `TokenBucketRateLimiter`（CAS 实现），按 `migration.rate-limit-qps` 配置**进程级单一全局 QPS**（默认 500，设为 0 不限流），**按批 acquire**——每批 migrate 启动前获取 1 个令牌，限调度速率（每秒最多启动 N 个批）。多 run 并发时共享同一令牌桶，全局调度速率真正受控。

> **批内访问速率由业务自管**：框架无法看见业务在 `migrate` 内对中间件的实际访问次数，故不预设按租户/按访问次数限流。业务可通过控制批大小、批内并发来间接控制对中间件的访问速率。

### 通知器（MigrationNotifier）

切流成功后框架通过 `MigrationNotifier` 向源区/目标区发送 Kafka 通知（默认实现 `KafkaMigrationNotifier`，使用固定的 `migrated-out`/`migrated-in` 键）。业务可实现 `MigrationNotifier` 接口并声明为 Spring Bean 覆盖默认行为。

---

## 测试

```bash
mvn test
```

测试覆盖：
- **两阶段迁移**（ADR-0005）：CORE→SECONDARY 顺序、双通知（phase=CORE_CUTOVER/ALL_DONE）、CORE 失败不进 SECONDARY、SECONDARY 对账失败、resume 三态分发、已切流禁 rollback
- **引擎核心行为**：批级隔离（批间隔离 + 批内全失败）、孤儿批恢复、零失败硬规则、切流对账闸门、全流程编排、回滚方向无关（未切流态）、批间并发
- **对账闸门**：业务自证一致性的委托与异常容错（业务 checker 抛异常视为不通过）
- **状态层契约**：CheckpointStore 的 run/租户状态管理（InMemory + Jdbc 共享契约测试）
- **配置绑定**：RegionProperties 多 region 多中间件结构
- **客户端注册表**：按 (region, type, instance) 查表

测试用 fake SPI + 内存 store，不依赖真实中间件，秒级完成。

---

## 包结构

```
org.example.migration
├── spi/          业务插件接口（TenantMigrationTask / CutoverAction / MigrationContext）
│   └── result/   结果类型（MigrationResult / VerifyResult）
├── client/       多中间件客户端抽象（RegionClient + 6 子接口 + Registry +
│                 6 实现 + ClientFactory SPI）
├── engine/       框架内核（MigrationEngine / TenantBatcher / RetryStrategy /
│                 CheckpointStore / ReconciliationGate / TenantScanner /
│                 ReconciliationChecker / TokenBucketRateLimiter /
│                 MigrationNotifier / MigrationRequest）
├── config/       配置绑定与自动装配（RegionProperties / MigrationProperties /
│                 MigrationAutoConfiguration / RegionClientAutoConfiguration /
│                 MigrationInfrastructureConfiguration / *ClientFactory ×6）
├── shell/        Spring Shell 命令（MigrationCommands / ShellApplication /
│                 ShellAutoConfiguration / TaskRegistry）
├── domain/       领域模型（RegionName / ClientType / Direction / MigrationPhase / RunStatus / TenantStatus）
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
    redis:
      default: { host: ENC(...), port: 6379, password: ENC(...) }
    ...
```

主密钥通过环境变量 `JASYPT_MASTER_PASSWORD` 注入。
