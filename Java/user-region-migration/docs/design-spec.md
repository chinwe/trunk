# 用户区域迁移框架（user-region-migration）设计文档

> 本设计是一个**迁移框架**，非端到端业务实现。各业务组件在本框架内自行开发核心数据迁移逻辑。
>
> 目标场景：SaaS 服务将新加坡大区的缅甸国家独立迁移到缅甸大区，涉及用户数据迁移。框架长期保留，后续其他区域拆分复用。

---

## 一、定位与边界

### 1.1 本工具职责（框架内核）

- **多数据源通用迁移能力**：MySQL / Redis / ES / S3 / DynamoDB / Kafka 六类中间件的统一客户端抽象
- **租户维度分片驱动**：框架按租户分批（默认 50/批，可配），业务插件接收租户批后自查询自迁移
- **可靠性机制**：分批 + 断点续传 + 并发 + 令牌桶限流 + Spring Retry 重试
- **迁移状态维护**：MySQL 状态表，批级断点 + 批级隔离
- **回滚**：方向无关迁移原语，回滚 = 源/目标对调的同一流程
- **切流协调**：migrate 内置总量对账闸门 + CutoverAction SPI（业务实现踢登录）+ Kafka 通知

### 1.2 不属于本工具（明确边界）

- ❌ **增量同步**（CDC / binlog 双写追平）—— 由中间件团队负责
- ❌ **次核心业务数据搬运** —— 业务按租户自行迁移
- ❌ **单租户内跨中间件的补偿回滚** —— 业务插件自管（框架捕获异常并标记 FAILED）

### 1.3 与业务应用层的协同时序（两阶段，ADR-0005）

```
[中间件团队] 启动 CDC 增量同步（双写期开始）
       ↓
[本工具]    CORE 阶段：搬运核心数据（基本信息/订单/账户余额）
       ↓
[本工具]    对账(CORE)：源=目标？
       ↓ 一致
[本工具]    切流：CutoverAction 踢登录 + Kafka 通知[phase=CORE_CUTOVER] → 路由切换
       ↓
[业务]     用户已在目标区登录 → 次核心数据短时缺失可降级
       ↓
[本工具]    SECONDARY 阶段：搬运次核心数据（行为日志/收藏/通知）
            （窗口期内用户在目标区写入的新次核心数据不会被覆盖——UPSERT+时间戳 merge）
       ↓
[本工具]    对账(SECONDARY) → Kafka 通知[phase=ALL_DONE]
       ↓
[中间件团队] 停源区写入，收尾
```

### 1.4 技术基线（沿用仓库现代约定）

- Spring Boot **3.2.12** + JDK **21** + **Maven**
- `spring-shell-starter`（仓库无先例，新增）
- `groupId: org.example`，`artifactId: user-region-migration`
- 参考样例：`Java/JUnit5Example/pom.xml`

---

## 二、核心设计决策（grilling 结论）

| # | 决策点 | 结论 | 关键理由 |
|---|--------|------|----------|
| 1 | 工具形态 | 框架 + SPI，非端到端实现 | 长期复用，多区域拆分复用 |
| 2 | 框架范式 | **自研轻量 SPI 框架**（不用 Spring Batch） | SPI 贴合业务，可靠性可控，避免 Spring Batch 认知与集成负担 |
| 3 | 回滚模型 | **方向无关迁移原语**：回滚=源/目标对调的同一 migrate | 业务只需实现一个 migrate 方法，消除 migrate/rollback 重复 |
| 4 | 状态存储 | **MySQL 状态表** | ACID、可查可运维、团队熟 |
| 5 | 配置策略 | **YAML + Jasypt 加密 + dev/test/prod 多 profile** | 自包含，密文可提交 git |
| 6 | SPI 风格 | **租户分片驱动式（两阶段）**：框架按 batchSize 切批，每批一次 `migrate(ctx, List<tenantId>, ..., MigrationPhase phase)` | 跨中间件差异大，统一 extract/load 削足适履；业务可批量查询/批量写入优化吞吐；CORE/SECONDARY 分流搬不同数据集 |
| 7 | 断点粒度 | **批级** | 批是状态原子单元；崩溃恢复以批为单位（ADR-0004） |
| 8 | 失败隔离 | **批级**：批 migrate 抛异常 → 整批标 FAILED | 批是补偿单元，框架不假设批内可按租户拆分（ADR-0004） |
| 9 | product/bizLine | **命令行自由参数透传** | 框架不预定义枚举，业务自解读 |
| 10 | 多数据源 | **配置驱动自动注册 + 多实例** | 加新 region 只改 yml，零 Java 改动；MySQL/Redis 用四参 API 指定实例名，ES/S3/DynamoDB/Kafka 三参 |
| 11 | 并发 | **批间并发**（threads 个批并行），单批内串行；见 ADR-0004 | 吞吐与安全平衡；批是业务调用与状态原子单元 |
| 12 | 限流 | **框架内置令牌桶**（进程级单一全局 QPS），**按批 acquire**（每批 1 个令牌，限调度速率） | 防压垮源/目标；批内中间件访问速率由业务自管 |
| 13 | 重试 | Spring Retry，max-attempts=3 指数退避，仅瞬时性异常（黑名单排除编程错误） | 业务数据异常不重试直接 FAILED |
| 14 | 事务边界 | 业务多中间件写入自管补偿；状态表 `createRun` 单事务 | 跨中间件无分布式事务 |
| 15 | 对账 | 可选 verify 钩子 + 独立 verify 命令；**migrate 切流前内置对账闸门** | 业务自证一致性（ADR-0001），框架只接受二值判定 |
| 16 | 切流 | **两阶段自动切流**（ADR-0005）：CORE 搬完→对账(CORE)→CutoverAction 踢登录→Kafka 通知[CORE_CUTOVER]→SECONDARY 继续搬→对账(SECONDARY)→通知[ALL_DONE] | 缩短用户影响窗口（只在 CORE 阶段被堵），核心数据先行可用 |
| 17 | 踢登录 | **CutoverAction SPI**（业务实现） | 依赖业务会话机制，框架不猜 |
| 18 | 正向迁移语义 | **真搬迁（删源区）** | 由业务 migrate 自决，框架方向无关 |
| 19 | rollback 方法 | **不单独定义**，复用 migrate | 方向无关原则，业务只实现 migrate |
| 20 | 示例 | 带**简化 UserMigration** 参考插件（MySQL 单中间件 mock 数据，**幂等 UPSERT**） | 验证框架可用 + 业务模板 |
| 21 | 租户来源 | **TenantScanner SPI + 命令行 --tenants** | --tenants 手动指定优先；为空时从源区自动扫描（MySQL 默认实现） |
| 22 | 对账计数 | **ReconciliationChecker SPI**（ADR-0001） | 业务自证一致性；已注册则 `CheckerReconciliationGate` 校验通过才切流；未注册则 `AlwaysPassReconciliationGate` 默认通过 |
| 23 | 幂等 | **业务 migrate 必须幂等**（ADR-0002） | resume/retry/rollback 都会重做同一租户，幂等是可恢复/可逆的数学前提 |
| 24 | 孤儿恢复 | **resume 视同 PENDING 重做 RUNNING 批** | 崩溃在 RUNNING 中间态的批不再成为孤儿；依赖 #23 幂等保证重做安全 |
| 25 | 部分失败切流 | **零失败硬规则**：有 FAILED 则不切流、Run 标 FAILED | 切流不可逆，跳过失败租户切流等于赌"该租户不重要" |

### 2.1 风险记录（grilling 中明确标记的权衡）

- **R1 [已缓解]**：migrate 不自深度验证，故障发现滞后 → 切流前对账闸门（业务自证，ADR-0001）缓解
- **R2 [已缓解]**：migrate 内部自动切流，搬错数据即切流的灾难风险 → 闸门拦截 + 零失败硬规则（决策 #25）双重保护
- **R3 [约束]**：方向无关要求业务 migrate 不得硬编码 region，否则回滚失效 → 通过文档/契约测试强约束
- **R4 [约束]**：业务 migrate 必须幂等，否则 resume/retry/rollback 产生重复数据 → SPI 契约 + 示例强约束（ADR-0002）

---

## 三、核心抽象设计

### 3.1 核心 SPI（业务插件实现此接口）

两阶段（ADR-0005）：框架对每次 Run 先驱动全批 CORE、切流后再驱动全批 SECONDARY。业务按 `MigrationPhase` 参数分流。

```java
package org.example.migration.spi;

public interface TenantMigrationTask {
    /** 任务标识，如 "user-migration" */
    String taskName();

    /**
     * 唯一必须实现的方法 —— 方向无关、按阶段分流。
     * 契约：实现必须基于 ctx.sourceRegion() / ctx.targetRegion()，禁止硬编码具体 region。
     *   正向迁移：source=源区, target=目标区
     *   逆向回滚：source=原目标区, target=原源区（框架对调注入）
     *
     *   phase=CORE：搬核心数据（登录后立即必需）。CORE 在切流之前，目标区无用户写入，写冲突无顾虑。
     *   phase=SECONDARY：搬次核心数据（切流后执行）。用户已在目标区写入，
     *                   必须用 UPSERT+时间戳 merge，不得简单覆盖（框架不假设源区冻结）。
     */
    MigrationResult migrate(MigrationContext ctx, List<String> tenantIds,
                            String product, String bizLine, MigrationPhase phase);

    /** 可选：深度对账，默认未实现。业务可覆盖做总量/抽样/逐条校验。 */
    default VerifyResult verify(MigrationContext ctx, List<String> tenantIds,
                                String product, String bizLine) {
        return VerifyResult.unimplemented();
    }
}
```

### 3.2 切流 SPI（业务插件实现踢登录等切流动作）

**幂等契约（ADR-0005）**：evict 必须幂等——对同一批租户调用 N 次与 1 次结果等价。CORE 切流后框架可能因崩溃恢复重做 evict。

```java
package org.example.migration.spi;

public interface CutoverAction {
    /** 切流时框架调用。业务实现踢登录、清理会话等切流动作。必须幂等（删 Redis token / 调"删除会话"语义鉴权接口）。 */
    void evict(MigrationContext ctx, List<String> tenantIds, String product, String bizLine);
}
```

### 3.3 迁移上下文（框架注入，业务拿客户端）

```java
package org.example.migration.spi;

public interface MigrationContext {
    RegionName sourceRegion();
    RegionName targetRegion();

    /** 单实例中间件（ES/S3/DynamoDB/Kafka）走三参，内部转 instance="default" */
    <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz);

    /** 多实例中间件（MySQL/Redis）走四参，显式指定实例名（如 "business"、"session"） */
    <C extends RegionClient> C client(RegionName region, ClientType type, String instance, Class<C> clazz);

    MigrationProperties config();   // batch-size / threads / rate-limit 等
}
```

### 3.4 多中间件客户端抽象（sealed interface）

```java
package org.example.migration.client;

public interface RegionClient {
    /** 逃生口：拿原生客户端做框架未封装的复杂操作 */
    Object raw();
}

public interface MySqlClient extends RegionClient {
    List<?> queryByTenants(String sql, List<String> tenantIds);
    int[] batchUpdate(String sql, List<Object[]> argsList);
    int deleteByTenants(String sql, List<String> tenantIds);
}
// RedisClient / EsClient / S3Client / DynamoDbClient / KafkaClient 同理，各暴露迁移高频操作

// ClientType 枚举位于 org.example.migration.domain 包
public enum ClientType { MYSQL, REDIS, ES, S3, DYNAMODB, KAFKA }
```

---

## 四、状态表设计（MySQL）

```sql
-- 一次迁移执行
CREATE TABLE migration_run (
    run_id            VARCHAR(64) PRIMARY KEY,          -- 如 user-migration-run-001
    task_name         VARCHAR(128) NOT NULL,            -- 如 user-migration
    direction         VARCHAR(16) NOT NULL,             -- FORWARD / ROLLBACK
    source_region     VARCHAR(32) NOT NULL,             -- singapore / myanmar
    target_region     VARCHAR(32) NOT NULL,
    product           VARCHAR(64),
    biz_line          VARCHAR(64),
    status            VARCHAR(16) NOT NULL,             -- RUNNING/DONE/FAILED
    total_tenants     INT,
    processed_tenants INT DEFAULT 0,
    failed_tenants    INT DEFAULT 0,
    started_at        DATETIME NOT NULL,
    updated_at        DATETIME NOT NULL,
    error_context     TEXT,
    parent_run_id     VARCHAR(64),                      -- 回滚 run 指向原正向 run
    INDEX idx_status (status),
    INDEX idx_task (task_name)
);

-- 租户级断点（每个租户一行）
CREATE TABLE migration_tenant_state (
    run_id          VARCHAR(64) NOT NULL,
    tenant_id       VARCHAR(64) NOT NULL,
    status          VARCHAR(16) NOT NULL,               -- PENDING/RUNNING/DONE/FAILED
    error_context   TEXT,
    updated_at      DATETIME NOT NULL,
    PRIMARY KEY (run_id, tenant_id),
    INDEX idx_run_status (run_id, status)
);
```

---

## 五、配置结构（application.yml + Jasypt）

```yaml
spring:
  application:
    name: user-region-migration
  main:
    web-application-type: none
  shell:
    interactive:
      enabled: false

regions:
  singapore:
    mysql:
      business: { jdbc-url: ENC(...), username: ENC(...), password: ENC(...) }
    redis:
      default: { host: ENC(...), port: 6379, password: ENC(...) }
    elasticsearch: { hosts: ENC(...), credentials: ENC(...) }
    s3: { endpoint: ENC(...), bucket: ENC(...), access-key: ENC(...), secret-key: ENC(...) }
    dynamodb: { endpoint: ENC(...), region: ap-southeast-1, access-key: ENC(...), secret-key: ENC(...) }
    kafka: { brokers: ENC(...), topic-prefix: singapore }
  myanmar:
    mysql: { ... }
    # ...

migration:
  default-batch-size: 50              # 业务调用粒度：每批一次 task.migrate，可批量查询/写入
  default-threads: 4                  # 批间并发度
  tenant-timeout-minutes: 30
  rate-limit-qps: 500                 # 进程级单一全局 QPS（令牌桶按批 acquire）
  retry:
    max-attempts: 3
    backoff-initial: 1s

jasypt:
  encryptor:
    password: ${JASYPT_MASTER_PASSWORD}   # 主密钥走环境变量

# spring.profiles.active 通过命令行 --spring.profiles.active=prod 切换
```

---

## 六、命令集（Spring Shell）

```
migrate    --task <name> --source <region> --target <region>
           --product <p> --biz-line <b>
           [--tenants <t1,t2,...>] [--batch-size 50] [--threads 4]
  # --tenants 手动指定租户ID列表,逗号分隔;不填则调用 TenantScanner 自动扫描源区
  # --batch-size 业务调用粒度:每批一次 task.migrate(ctx, batchTenantIds, ...)
  # --threads 批间并发度:threads 个批并行,单批内串行
  # 流程：① 按 batchSize 切批,批间并发搬运(批级隔离,批级断点)
  #      ② 零失败硬规则:有 FAILED 批则不切流,Run 标 FAILED
  #      ③ 全 DONE → 跑对账闸门(由 ReconciliationChecker SPI 业务自证一致性)
  #      ④ 对账通过 → CutoverAction.evict(踢登录) → Kafka 迁出/迁入通知
  #      ⑤ 对账不通过 → 停下不切流,等人工介入

rollback   --run-id <id> --task <name>
  # 框架读原 run 元数据,创建新 run,direction=ROLLBACK,source/target 对调
  # 复用全部基础设施调用同一个 task.migrate(批粒度)

resume     --run-id <id> --task <name>
  # 重做 PENDING + 卡在 RUNNING 的批(孤儿恢复,ADR-0002 幂等保证安全)

verify     --run-id <id> --task <name>
  # 调 task.verify 钩子

status     --run-id <id>
  # 查询单次执行的详细状态

tasks
  # 列出已注册的 MigrationTask 业务插件

dry-run    --source <region> --target <region> [--tenants <t1,t2,...>]
  # 预估待迁移租户数（不写数据），--tenants 不填则自动扫描源区
```

---

## 七、可靠性机制总结

| 机制 | 实现方式 | 备注 |
|------|----------|------|
| 分批 | 按 batchSize 切批，每批一次 task.migrate | 业务调用粒度 + 状态原子单元（ADR-0004） |
| 断点续传 | migration_tenant_state 记批级断点；resume 重做 PENDING + RUNNING 批 | 崩溃从未处理/孤儿批恢复 |
| 并发 | 批间并发：threads 个批并行，单批内串行 | threads 个批真正并行（ADR-0004） |
| 限流 | 进程级单一令牌桶，按批 acquire（每批 1 个令牌，限调度速率） | 防压垮源/目标；批内访问速率业务自管 |
| 重试 | Spring Retry，max 3 次指数退避，黑名单排除编程错误 | 业务数据异常不重试直接 FAILED |
| 批级隔离 | 每批 try-catch，整批 FAILED 记 error_context；状态写入失败不外泄 | 其余批继续 |
| 事务边界 | 业务多中间件自管补偿；状态表 createRun 单事务 | 跨中间件无分布式事务 |
| 切流闸门 | 零失败硬规则 + 业务自证对账（ADR-0001），不通过不切 | 防搬错数据自动切流灾难 |
| 幂等 | SPI 契约强制 migrate 幂等（ADR-0002） | 可恢复/可逆的数学前提 |
| 回滚 | 方向无关，source/target 对调复用 migrate | 业务只实现一个 migrate |

---

## 八、migrate 完整流程

```
migrate 命令内部编排:
  ① 创建 migration_run(status=RUNNING) + 初始化 migration_tenant_state(所有租户 PENDING)  -- 单事务
  ② 按 batchSize 切批,批间并发(threads 个批并行,单批内串行):
       for batch in batches:
         try { 批内所有租户置 RUNNING; 限流 acquire(1);  # 按批限流
               task.migrate(ctx, batchTenantIds, product, bizLine);  -- 业务须幂等(ADR-0002)
               批内所有租户置 DONE; processed += batchTenantIds.size() }
         catch(e) { 批内所有租户置 FAILED + error_context; failed += batchTenantIds.size() }  # 批级隔离
       (状态写入本身失败时记日志,不让单批炸整个 run)
  ③ 全部完成 → 零失败硬规则检查:
       if failed > 0: migration_run.status=FAILED, 不切流, 等人工/resume  # 决策 #25
  ④ 全 DONE → 跑对账闸门(ReconciliationChecker 业务自证一致性, ADR-0001)
  ⑤ 闸门通过:
       CutoverAction.evict(ctx, allTenants, ...)     # 业务踢登录
       Kafka 发迁出通知(源区) + 迁入通知(目标区)
       migration_run.status=DONE
  ⑥ 闸门不通过:
       migration_run.status=FAILED,不切流,等人工介入
```

---

## 九、回滚流程（方向无关）

```
rollback --run-id <id>:
  ① 读取原 run(source=Sg, target=Mm, direction=FORWARD)
  ② 创建新 run(source=Mm, target=Sg, direction=ROLLBACK, parent_run_id=<原 run>)
  ③ 从原 run 的 tenant_state 读取所有 status=DONE 的租户(只有这些需要回滚)
  ④ 按 batchSize 切批,对每批调用同一个 task.migrate(ctx, batchTenantIds, ...)
     — 此时 ctx.sourceRegion()=Mm, ctx.targetRegion()=Sg(框架对调注入)
     — 业务 migrate 内部"从 source 读→写 target→删 source"逻辑天然反向执行
  ⑤ 同样支持断点续传、批级隔离、状态维护
  ⑥ 回滚完成后同样跑对账闸门,对得上才发"回滚完成"Kafka 通知
```

---

## 十、包结构与脚手架交付物

```
org.example.migration
├── spi/                    # 业务插件接口
│   ├── TenantMigrationTask.java
│   ├── CutoverAction.java
│   ├── MigrationContext.java
│   └── result/ (MigrationResult, VerifyResult)
├── client/                 # 多中间件客户端抽象
│   ├── RegionClient.java
│   ├── MySqlClient.java, RedisClient.java, EsClient.java
│   ├── S3Client.java, DynamoDbClient.java, KafkaClient.java
│   ├── JdbcMySqlClient.java, SpringRedisClient.java, ElasticEsClient.java
│   ├── AwsS3Client.java, AwsDynamoDbClient.java, SpringKafkaClient.java
│   ├── RegionClientRegistry.java
│   └── ClientFactory.java              # 客户端工厂 SPI
├── engine/                 # 框架内核
│   ├── MigrationEngine.java            # 编排：批间并发/断点/对账/切流
│   ├── TenantBatcher.java              # 批间并发调度（ADR-0004）
│   ├── RegistryMigrationContext.java   # MigrationContext 统一实现
│   ├── RetryStrategy.java              # 重试策略（瞬时性异常重试，编程错误排除）
│   ├── CheckpointStore.java            # 状态表读写（抽象）
│   ├── JdbcCheckpointStore.java        # JDBC 实现（MySQL/H2，createRun 单事务）
│   ├── InMemoryCheckpointStore.java    # 内存实现（测试用）
│   ├── ReconciliationGate.java         # 对账闸门（抽象）
│   ├── AlwaysPassReconciliationGate.java  # 默认通过（无 Checker 时）
│   ├── CheckerReconciliationGate.java  # 委托业务自证一致性（ADR-0001）
│   ├── ReconciliationChecker.java      # 对账校验器 SPI（业务实现）
│   ├── TokenBucketRateLimiter.java     # 令牌桶限流（进程级单一全局 QPS）
│   ├── MigrationNotifier.java          # 迁移通知器（抽象）
│   ├── KafkaMigrationNotifier.java     # Kafka 通知实现
│   ├── TenantScanner.java              # 租户扫描器（抽象 + MySQL 实现）
│   └── MigrationRequest.java           # 迁移请求参数
├── config/                 # 配置绑定与自动装配
│   ├── RegionProperties.java
│   ├── MigrationProperties.java
│   ├── MigrationAutoConfiguration.java
│   ├── RegionClientAutoConfiguration.java
│   ├── MigrationInfrastructureConfiguration.java
│   └── *ClientFactory.java ×6          # 6 个中间件客户端工厂（实现 ClientFactory SPI）
├── shell/                  # Spring Shell 命令
│   ├── MigrationCommands.java          # 全部 7 个命令（migrate/resume/rollback/verify/status/tasks/dry-run）
│   ├── ShellApplication.java           # @SpringBootApplication 入口
│   ├── ShellAutoConfiguration.java     # TaskRegistry 自动收集
│   └── TaskRegistry.java               # 任务注册表
├── domain/                 # 领域模型
│   ├── RegionName.java, ClientType.java, Direction.java, RunStatus.java, TenantStatus.java
│   └── entity/ (MigrationRun, MigrationTenantState)
└── example/                # 参考实现（简化 UserMigration）
    ├── UserMigrationTask.java           # 实现 TenantMigrationTask, MySQL 单中间件 mock
    └── UserCutoverAction.java           # 实现 CutoverAction, mock 踢登录
```

**额外交付**：
- `pom.xml`（Spring Boot 3.2.12 + JDK 21 + spring-shell-starter + jasypt + 各中间件 SDK + JaCoCo 90% 覆盖率）
- `application.yml` + `application-dev/test/prod.yml`
- `src/main/resources/schema.sql`（状态表 DDL）
- `README.md`（框架使用说明 + 业务插件开发指南）

### 实现与设计差异说明

以下为实施过程中基于工程实际的调整：

1. **RegionClient 非 sealed**：Spring Boot 的 CGLIB 代理与 sealed interface 不兼容，改为普通 interface
2. **引擎组件整合**：设计稿的 `CutoverCoordinator` 未作为独立类——`MigrationEngine` 直接内置切流收尾（`finalizeAfterMigration()`）。`TenantBatcher` 作为独立类实现并发逻辑，由 `MigrationEngine` 委托调用。
3. **Shell 命令合一**：7 个命令合并在 `MigrationCommands.java` 中——命令间共享装配逻辑（`buildEngine`/`resolveTenants`），独立文件反而分散
4. **新增 TenantScanner SPI**：`migrate --tenants` 为空时自动扫描源区租户，提供 MySQL 默认实现
5. **新增 ReconciliationChecker SPI**（ADR-0001，原 ReconciliationCounter 已撤销）：对账由业务自证一致性，`CheckerReconciliationGate` 调用业务 `consistent()`；未提供时 `AlwaysPassReconciliationGate` 默认通过
6. **新增 MigrationNotifier 抽象**：切流后通知可替换（默认 Kafka），解耦引擎与通知方式
7. **多实例支持**：MySQL/Redis 通过四参 API（`ctx.client(region, type, instance, clazz)`）支持同一 region 多个命名实例；ES/S3/DynamoDB/Kafka 维持三参

### 第二轮 grilling 修订（2026-07-05）

经第一性原理审核发现的核心缺陷修订，详见 `docs/adr/0001`、`0002`、`0003`：

8. **对账闸门语义重定义**（C1）：原 `CountReconciliationGate` 在"真搬迁（删源）"语义下与 SPI 契约自相矛盾（源 count≈0、目标 count=N，永远不等）。改为 `ReconciliationChecker` 业务自证一致性。
9. **零失败切流硬规则**（C2）：原实现在有 FAILED 租户时仍走闸门并标 DONE，与 `RunStatus` 注释矛盾。改为有 FAILED 则不切流、Run 标 FAILED。
10. **孤儿租户恢复**（C3）：原 `migrateSingleTenant` 三步翻转引入 RUNNING 中间态，JVM 在中间崩溃则租户永久卡 RUNNING。改为 `resume` 视同 PENDING 重做 RUNNING 租户。
11. **幂等契约强制**（C4）：原 SPI 契约未声明幂等要求，示例用非幂等 INSERT。改为 SPI 契约强制 migrate 幂等，示例改 UPSERT。
12. **并发模型重构**（R2，ADR-0003，**后被 ADR-0004 撤销**）：原"批次间并发、单批内串行"导致 threads 形同虚设。一度改为租户级并发。**第三轮恢复批粒度**（ADR-0004），批间并发、批内串行，batch 升回业务调用粒度。
13. **撤销按类型限流设想**（D1）：原 `rate-limit.{type}.qps` 配置项从未被读取，误导用户。删除配置项，限流改为进程级单一全局 QPS。
14. **状态层事务与隔离补齐**（R1/R4）：`createRun` 加单事务；批级状态写入失败不外泄为整 run 异常。
15. **生产准备补齐**：MySQL 工厂换 HikariCP 连接池（Q5）；删除 Jasypt 默认密钥（Q4）。

### 第三轮修订（2026-07-05，批粒度对齐）

经对齐 SPI 本意（决策 #6 "租户分片驱动"、`migrate(List<tenantIds>)` 签名），发现 ADR-0003 的"租户级并发"基于"业务拿单租户"的错误前提。详见 ADR-0004：

16. **批粒度迁移**：业务一次拿一批（batchSize 可配），可批量查询/批量写入优化吞吐。
17. **批级失败隔离**：批 migrate 抛异常 → 整批标 FAILED。批是原子单元（业务在批内自管补偿）。
18. **批间并发**：threads 个批并行，单批内串行（撤销 ADR-0003 的租户级并发）。
19. **限流按批 acquire**：每批 acquire 1 个令牌（限调度速率），批内访问速率由业务自管。

---

## 十一、待业务方明确的开放点（不影响框架搭建）

以下不影响框架内核搭建，业务插件开发时各自确认：

1. 四类核心数据（用户/租户/设备/Open）各自落在哪些中间件、表结构如何
2. "Open" 的精确语义
3. 单租户平均数据体量（用于 dry-run 耗时预估）
4. CutoverAction 踢登录的具体会话机制（Redis token？鉴权接口？）

---

## 十二、实施顺序建议

1. **脚手架**：pom.xml + 包结构 + ShellApplication 入口 + application.yml 多 profile
2. **配置层**：RegionProperties + Jasypt + RegionClientAutoConfiguration
3. **客户端层**：RegionClient + 六个子接口 + Registry
4. **状态层**：状态表 DDL + entity + CheckpointStore
5. **SPI 层**：TenantMigrationTask + CutoverAction + MigrationContext
6. **引擎层**：MigrationEngine + TenantBatcher + RateLimiter + RetryStrategy
7. **命令层**：六个 Spring Shell 命令
8. **示例**：简化 UserMigrationTask + UserCutoverAction
9. **README + 业务插件开发指南**

---

## 附录：grilling 决策溯源

本设计经两轮 grilling 拷问形成，关键决策的推理过程记录如下：

- **为何不用 Spring Batch**：它的断点粒度（Step/Chunk）与"租户级 + 多中间件 + 跨区多源多目标"模型对不上；retry/skip 面向单条失败，与跨区网络/双区一致性场景层次不同；概念面（Job/Step/Chunk/...）对业务插件认知负担高；与 Spring Shell 交互式长期运维心智冲突。
- **为何方向无关（单 migrate 方法）**：用户洞察"流程一样，数据源目标对调"——回滚与正向是同一流程，仅 Source/Sink 对调。消除 migrate/rollback 90% 重复代码，业务只实现一个方法。
- **为何 migrate 内置对账闸门**：migrate 不自深度验证 + 内部自动切流，两者组合会产生"搬错数据即切流"的灾难。闸门是关键拦截点。**第二轮修订**：闸门算法由业务自证（ADR-0001），框架不预设 count 比对——因真搬迁语义下 count 比对数学上不自洽。
- **为何租户分片驱动而非 extract/load**：不同业务（用户/设备/Open）查询条件、跨中间件搬运逻辑差异大，统一 extract/load 抽象削足适履；让业务拿租户批自发挥更务实。
- **为何强制 migrate 幂等**（第二轮新增）：resume/retry/rollback 都会重做同一租户，幂等是可恢复/可逆承诺的数学前提。非幂等 migrate 在第一次 retry/resume 时即可能产生重复数据。
- **为何批粒度迁移而非单租户**（第三轮新增，ADR-0004）：SPI 签名 `migrate(List<tenantIds>)` 与决策 #6 "租户分片驱动"本意都是业务拿一批。批粒度让业务能利用中间件批量 API（批量查询、批量 UPSERT）大幅提升吞吐；ADR-0003 的"租户级并发"基于当时"业务实际拿单租户"的实现现状，方向纠正错误，现撤销。批间并发、批内串行，threads 控制批并行度。
