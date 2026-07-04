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
- **迁移状态维护**：MySQL 状态表，租户级断点 + 单租户隔离
- **回滚**：方向无关迁移原语，回滚 = 源/目标对调的同一流程
- **切流协调**：migrate 内置总量对账闸门 + CutoverAction SPI（业务实现踢登录）+ Kafka 通知

### 1.2 不属于本工具（明确边界）

- ❌ **增量同步**（CDC / binlog 双写追平）—— 由中间件团队负责
- ❌ **次核心业务数据搬运** —— 业务按租户自行迁移
- ❌ **单租户内跨中间件的补偿回滚** —— 业务插件自管（框架捕获异常并标记 FAILED）

### 1.3 与业务应用层的协同时序

```
[中间件团队] 启动 CDC 增量同步（双写期开始）
       ↓
[本工具]   搬运存量核心数据（存量 + 增量共同追平）
       ↓
[本工具]   对账：源=目标？
       ↓ 一致
[本工具]   切流：总量闸门 → CutoverAction 踢登录 → Kafka 通知 → 路由切换
       ↓
[业务]     收到 Kafka 通知，清内存数据，重定向到缅甸
       ↓
[中间件团队] 停源区写入，收尾
```

### 1.4 技术基线（沿用仓库现代约定）

- Spring Boot **3.2.10** + JDK **21** + **Maven**
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
| 6 | SPI 风格 | **租户分片驱动式**：框架分租户批，业务自查询 | 跨中间件差异大，统一 extract/load 削足适履 |
| 7 | 断点粒度 | **租户级** | 崩溃恢复精确 |
| 8 | 失败隔离 | **单租户隔离** | 失败租户记 FAILED，整批其余继续 |
| 9 | product/bizLine | **命令行自由参数透传** | 框架不预定义枚举，业务自解读 |
| 10 | 多数据源 | **sealed RegionClient + 配置驱动自动注册** | 加新 region 只改 yml，零 Java 改动 |
| 11 | 并发 | **批次间并发**（可配线程池），单批内串行 | 吞吐与安全平衡 |
| 12 | 限流 | **框架内置令牌桶**（按中间件类型分别配 QPS） | 防压垮源/目标 |
| 13 | 重试 | Spring Retry，max-attempts=3 指数退避，仅瞬时性异常 | 业务数据异常不重试直接 FAILED |
| 14 | 事务边界 | 业务多中间件写入自管补偿；状态表独立小事务 | 跨中间件无分布式事务 |
| 15 | 对账 | 可选 verify 钩子 + 独立 verify 命令；**migrate 切流前内置总量闸门** | migrate 不深度自验，但切流前廉价闸门拦截明显丢失 |
| 16 | 切流 | **migrate 内部自动**（搬数据→总量闸门→CutoverAction→Kafka） | 单命令完成全流程，闸门保安全 |
| 17 | 踢登录 | **CutoverAction SPI**（业务实现） | 依赖业务会话机制，框架不猜 |
| 18 | 正向迁移语义 | **真搬迁（删源区）** | 由业务 migrate 自决，框架方向无关 |
| 19 | rollback 方法 | **不单独定义**，复用 migrate | 方向无关原则，业务只实现 migrate |
| 20 | 示例 | 带**简化 UserMigration** 参考插件（MySQL 单中间件 mock 数据） | 验证框架可用 + 业务模板 |

### 2.1 风险记录（grilling 中明确标记的权衡）

- **R1 [已缓解]**：migrate 不自深度验证，故障发现滞后 → 切流前加总量对账闸门缓解
- **R2 [已缓解]**：migrate 内部自动切流，搬错数据即切流的灾难风险 → 闸门拦截，对账不通过不切流
- **R3 [约束]**：方向无关要求业务 migrate 不得硬编码 region，否则回滚失效 → 通过文档/契约测试强约束

---

## 三、核心抽象设计

### 3.1 核心 SPI（业务插件实现此接口）

```java
package org.example.migration.spi;

public interface TenantMigrationTask {
    /** 任务标识，如 "user-migration" */
    String taskName();

    /**
     * 唯一必须实现的方法 —— 方向无关。
     * 契约：实现必须基于 ctx.sourceRegion() / ctx.targetRegion()，禁止硬编码具体 region。
     *   正向迁移：source=源区, target=目标区
     *   逆向回滚：source=原目标区, target=原源区（框架对调注入）
     */
    MigrationResult migrate(MigrationContext ctx, List<String> tenantIds,
                            String product, String bizLine);

    /** 可选：深度对账，默认未实现。业务可覆盖做总量/抽样/逐条校验。 */
    default VerifyResult verify(MigrationContext ctx, List<String> tenantIds,
                                String product, String bizLine) {
        return VerifyResult.unimplemented();
    }
}
```

### 3.2 切流 SPI（业务插件实现踢登录等切流动作）

```java
package org.example.migration.spi;

public interface CutoverAction {
    /** 切流时框架调用：业务实现踢登录、清理会话等切流动作 */
    void evict(MigrationContext ctx, List<String> tenantIds, String product, String bizLine);
}
```

### 3.3 迁移上下文（框架注入，业务拿客户端）

```java
package org.example.migration.spi;

public interface MigrationContext {
    RegionName sourceRegion();
    RegionName targetRegion();
    <C extends RegionClient> C client(RegionName region, ClientType type);
    MigrationConfig config();   // batch-size / threads / rate-limit 等
}
```

### 3.4 多中间件客户端抽象（sealed interface）

```java
package org.example.migration.client;

public sealed interface RegionClient
        permits MySqlClient, RedisClient, EsClient, S3Client, DynamoDbClient, KafkaClient {
    /** 逃生口：拿原生客户端做框架未封装的复杂操作 */
    Object raw();
}

public interface MySqlClient extends RegionClient {
    <T> List<T> query(String sql, RowMapper<T> mapper, Object... args);
    int[] batchUpdate(String sql, List<Object[]> args);
}
// RedisClient / EsClient / S3Client / DynamoDbClient / KafkaClient 同理，各暴露迁移高频操作

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
    status            VARCHAR(16) NOT NULL,             -- INIT/RUNNING/PAUSED/DONE/FAILED
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
regions:
  singapore:
    mysql:
      business: { jdbc-url: ENC(...), username: ENC(...), password: ENC(...) }
    redis: { host: ENC(...), port: 6379, password: ENC(...) }
    elasticsearch: { hosts: ENC(...), credentials: ENC(...) }
    s3: { endpoint: ENC(...), bucket: ENC(...), access-key: ENC(...), secret-key: ENC(...) }
    dynamodb: { endpoint: ENC(...), region: ap-southeast-1, access-key: ENC(...), secret-key: ENC(...) }
    kafka: { brokers: ENC(...), topic-prefix: singapore }
  myanmar:
    mysql: { ... }
    # ...

migration:
  default-batch-size: 50
  default-threads: 4
  rate-limit:
    mysql: { qps: 1000 }
    s3: { qps: 200 }
    # 各中间件分别配
  retry:
    max-attempts: 3
    backoff-initial: 1s

jasypt:
  password: ${JASYPT_MASTER_PASSWORD}   # 主密钥走环境变量

# spring.profiles.active 通过命令行 --spring.profiles.active=prod 切换
```

---

## 六、命令集（Spring Shell）

```
migrate    --task <name> --source <region> --target <region>
           --product <p> --biz-line <b>
           [--batch-size 50] [--threads 4] [--dry-run] [--resume <run-id>]
  # 流程：① 按租户分批搬运(单租户隔离,租户级断点)
  #      ② 全量完成跑总量对账闸门(COUNT 级)
  #      ③ 对账通过 → CutoverAction.evict(踢登录) → Kafka 迁出/迁入通知
  #      ④ 对账不通过 → 停下不切流,等人工介入
  # --dry-run 只预估数据量不写

rollback   --run-id <id>
  # 框架读原 run 元数据,创建新 run,direction=ROLLBACK,source/target 对调
  # 复用全部基础设施调用同一个 task.migrate

resume     --run-id <id>
  # 等价 migrate --resume,从未处理租户继续

verify     --run-id <id> [--sample 0.1] [--full]
  # 调 task.verify 钩子;--sample 抽样,--full 全量

status     [--run-id <id>] [--task <name>]
  # 查询迁移状态;无参列所有 run;有 run-id 显示详情含失败租户

tasks
  # 列出已注册的 MigrationTask 业务插件
```

---

## 七、可靠性机制总结

| 机制 | 实现方式 | 备注 |
|------|----------|------|
| 分批 | 框架按租户分批，默认 50/批可配 | 租户维度 |
| 断点续传 | migration_tenant_state 记租户级断点 | 崩溃从未处理租户恢复 |
| 并发 | ThreadPoolTaskExecutor，批次间并发，单批串行 | 线程数可配 |
| 限流 | 令牌桶，按中间件类型分别配 QPS | 防压垮源/目标 |
| 重试 | Spring Retry，max 3 次指数退避，仅瞬时性异常 | 业务数据异常不重试直接 FAILED |
| 单租户隔离 | 每租户 try-catch，FAILED 记 error_context | 整批其余继续 |
| 事务边界 | 业务多中间件自管补偿；状态表独立小事务 | 跨中间件无分布式事务 |
| 切流闸门 | migrate 切流前跑总量对账，不通过不切 | 防搬错数据自动切流灾难 |
| 回滚 | 方向无关，source/target 对调复用 migrate | 业务只实现一个 migrate |

---

## 八、migrate 完整流程

```
migrate 命令内部编排:
  ① 创建 migration_run(status=RUNNING) + 初始化 migration_tenant_state(所有租户 PENDING)
  ② 按租户分批(50/批),批次间并发(线程池)
       每批:
         for tenant in batch(单批内串行):
           try { task.migrate(ctx, [tenant], product, bizLine);
                 更新 tenant_state.status=DONE; processed++ }
           catch(e) { tenant_state.status=FAILED + error_context; failed++ }  # 单租户隔离
  ③ 全部批次完成 → 跑总量对账闸门(各中间件 COUNT 源 vs 目标)
  ④ 闸门通过:
       CutoverAction.evict(ctx, allTenants, ...)     # 业务踢登录
       Kafka 发迁出通知(源区) + 迁入通知(目标区)
       migration_run.status=DONE
  ⑤ 闸门不通过:
       migration_run.status=FAILED,不切流,等人工介入
```

---

## 九、回滚流程（方向无关）

```
rollback --run-id <id>:
  ① 读取原 run(source=Sg, target=Mm, direction=FORWARD)
  ② 创建新 run(source=Mm, target=Sg, direction=ROLLBACK, parent_run_id=<原 run>)
  ③ 从原 run 的 tenant_state 读取所有 status=DONE 的租户(只有这些需要回滚)
  ④ 按租户分批,对每批调用同一个 task.migrate(ctx, [tenant], ...)
     — 此时 ctx.sourceRegion()=Mm, ctx.targetRegion()=Sg(框架对调注入)
     — 业务 migrate 内部"从 source 读→写 target→删 source"逻辑天然反向执行
  ⑤ 同样支持断点续传、单租户隔离、状态维护
  ⑥ 回滚完成后同样跑总量闸门,对得上才发"回滚完成"Kafka 通知
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
│   ├── RegionClient.java (sealed)
│   ├── MySqlClient.java, RedisClient.java, EsClient.java
│   ├── S3Client.java, DynamoDbClient.java, KafkaClient.java
│   └── RegionClientRegistry.java
├── engine/                 # 框架内核
│   ├── MigrationEngine.java        # 编排：分批/并发/断点/对账/切流
│   ├── TenantBatcher.java          # 租户分批
│   ├── CheckpointStore.java        # 状态表读写
│   ├── RateLimiter.java            # 令牌桶
│   └── CutoverCoordinator.java     # 切流协调(闸门+CutoverAction+Kafka)
├── config/                 # 配置绑定与自动装配
│   ├── RegionProperties.java
│   ├── MigrationProperties.java
│   └── RegionClientAutoConfiguration.java
├── shell/                  # Spring Shell 命令
│   ├── MigrateCommand.java, RollbackCommand.java
│   ├── ResumeCommand.java, VerifyCommand.java
│   ├── StatusCommand.java, TasksCommand.java
│   └── ShellApplication.java       # @SpringShellApplication 入口
├── domain/                 # 领域模型
│   ├── RegionName.java, ClientType.java, Direction.java, RunStatus.java
│   └── entity/ (MigrationRun, MigrationTenantState)
└── example/                # 参考实现（简化 UserMigration）
    ├── UserMigrationTask.java       # 实现 TenantMigrationTask, MySQL 单中间件 mock
    └── UserCutoverAction.java       # 实现 CutoverAction, mock 踢登录
```

**额外交付**：
- `pom.xml`（Spring Boot 3.2.10 + JDK 21 + spring-shell-starter + jasypt + 各中间件 SDK）
- `application.yml` + `application-dev/test/prod.yml`
- `src/main/resources/schema.sql`（状态表 DDL）
- `README.md`（框架使用说明 + 业务插件开发指南）

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
3. **客户端层**：sealed RegionClient + 六个子接口 + Registry
4. **状态层**：状态表 DDL + entity + CheckpointStore
5. **SPI 层**：TenantMigrationTask + CutoverAction + MigrationContext
6. **引擎层**：MigrationEngine + TenantBatcher + RateLimiter + CutoverCoordinator
7. **命令层**：六个 Spring Shell 命令
8. **示例**：简化 UserMigrationTask + UserCutoverAction
9. **README + 业务插件开发指南**

---

## 附录：grilling 决策溯源

本设计经一轮 grilling 拷问形成，关键决策的推理过程记录如下，便于后续回顾为何这样选：

- **为何不用 Spring Batch**：它的断点粒度（Step/Chunk）与"租户级 + 多中间件 + 跨区多源多目标"模型对不上；retry/skip 面向单条失败，与跨区网络/双区一致性场景层次不同；概念面（Job/Step/Chunk/...）对业务插件认知负担高；与 Spring Shell 交互式长期运维心智冲突。
- **为何方向无关（单 migrate 方法）**：用户洞察"流程一样，数据源目标对调"——回滚与正向是同一流程，仅 Source/Sink 对调。消除 migrate/rollback 90% 重复代码，业务只实现一个方法。
- **为何 migrate 内置总量闸门**：用户选择"migrate 不自深度验证 + 内部自动切流"，两者组合会产生"搬错数据即切流"的灾难。闸门是廉价（COUNT 级）但关键的拦截点。
- **为何租户分片驱动而非 extract/load**：不同业务（用户/设备/Open）查询条件、跨中间件搬运逻辑差异大，统一 extract/load 抽象削足适履；让业务拿租户批自发挥更务实。
