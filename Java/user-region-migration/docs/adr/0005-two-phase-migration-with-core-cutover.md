# 0005 — 两阶段迁移：核心数据先行切流

## Status

accepted (2026-07-05)

## Context

ADR-0001–0004 建立的是**单阶段、扁平**迁移模型：业务实现一个 `migrate(ctx, tenantIds, product, bizLine)`，
一次性搬完全部数据；框架在所有租户搬完后做一次对账、一次切流、一次 Kafka 通知。

实际业务数据跨多个中间件、量级悬殊。一次性迁移耗时长，**整段窗口期内用户都被堵在源区无法登录目标区**，
对业务影响窗口过大。真实诉求：**把数据分成"核心"（用户登录后立即必需）和"次核心"（短时缺失可容忍/可降级）两组，
核心数据搬完就立刻切流放用户进来，次核心数据在后台继续搬**。这样"用户被堵"的窗口只覆盖核心数据迁移时长，
而不是全部数据迁移时长。

引入两阶段后，单阶段模型的若干隐式假设（切流是一次性末尾动作、对账一次、状态机三态、CutoverAction 无幂等要求）
全部被打破。本 ADR 记录两阶段下的全部架构决策。

## Decision

### 1. SPI 加 `MigrationPhase` 参数（migrate / consistent 双侧同构）

`TenantMigrationTask.migrate` 和 `ReconciliationChecker.consistent` 各加一个 `MigrationPhase phase` 参数：

```java
enum MigrationPhase { CORE, SECONDARY }

MigrationResult migrate(MigrationContext ctx, List<String> tenantIds,
                        String product, String bizLine, MigrationPhase phase);

boolean consistent(MigrationRun run, List<String> migratedTenantIds, MigrationPhase phase);
```

业务一个方法、按 `phase` 分流（搬不同的表/中间件、对账不同的数据集）。**框架硬编码编排**，
不暴露 phase 序列配置——"核心迁完切流"是框架承诺，不是业务声明。

### 2. Run 级批量切流（非租户级、非批级）

切流粒度沿用 ADR-0004 之上的 Run 级：**全部租户的核心数据搬完 + 对账通过**，才一次性踢全部登录。
不是 per-tenant（颠覆现有架构、且引入"踢登录后次核心迁移期间用户重新登录"的死结），
不是 per-batch（与"批是状态原子单元"耦合过深）。

### 3. Run 状态机扩展为五态

```
RUNNING_CORE → CORE_CUTOVER_DONE → RUNNING_SECONDARY → DONE
任何阶段对账失败 / 零失败检查不过 → FAILED
```

- `RUNNING_CORE`：正在搬核心（取代原 `RUNNING`）
- `CORE_CUTOVER_DONE`：**中间态**。核心已对账 + 已踢登录 + 已发通知①；待进 SECONDARY。**对外可见**（`status` 命令直接展示枚举值，运维需理解此中间态）。
- `RUNNING_SECONDARY`：正在搬次核心
- `MigrationRun` 加 `phase` 字段记录当前阶段。

### 4. CORE 切流的执行顺序：`evict → 写 CORE_CUTOVER_DONE → 通知①`

**关键：先执行副作用，后写状态。** 理由——崩溃在"evict 后、写状态前"的窗口会让 resume 重做 evict；
崩溃在"写状态后、evict 前"（即反过来）会让 resume 跳过 evict。**漏踢 = 数据不一致（源区继续被写），
重踢 = 冗余（幂等则无害）**。漏踢比重踢危险得多（同 ADR-0002 "重做优于漏做"的逻辑）。

由此推出**新的硬契约：`CutoverAction.evict` 必须幂等**——对同一批租户调用 N 次与 1 次结果等价。
单阶段模型下此契约是隐式的（窗口极小），两阶段把它**显式化**（窗口被 SECONDARY 全程迁移放大，可达数小时）。
典型幂等实现：删 Redis token、调"删除会话"语义的鉴权接口。**非幂等的踢登录语义（如"触发推送通知用户被踢"）
会破坏本契约，业务不得使用。**

### 5. 窗口期职责全部在业务

`CORE_CUTOVER_DONE` 到 `RUNNING_SECONDARY` 完成之间是**窗口期**：用户已在目标区登录并读写，
同时框架还在从源区搬次核心数据。

- **读缺失**（用户读次核心数据读到空/部分）→ 应用层自降级（缓存/默认值/友好提示）。
- **写冲突**（用户在目标区新写次核心数据，SECONDARY migrate 从源区搬旧数据覆盖）→ 业务在 SECONDARY migrate 内实现 merge（推荐 UPSERT + `updated_at` 比较，或业务自定义策略）。

**框架不感知数据语义，不内建读保护，不暴露窗口期状态查询钩子。** 框架按最坏情况承担——
**不假设踢登录后源区冻结**（系统可能有后台 job / 异步任务 / 跨系统同步在踢登录后仍往源区写次核心数据）。
"低峰窗口期操作"是业务侧的运维安排，框架不依赖、不感知。

### 6. SECONDARY 对账失败 → FAILED 等人工；已切流态禁 rollback

`ReconciliationChecker.consistent(..., SECONDARY)` 不通过时，**Run 标 FAILED，停在原地等人工介入**，
不自动 rollback。

理由：CORE 切流是不可逆动作（Q4），SECONDARY 对账失败时用户已在目标区登录、可能已产生新数据。
自动 rollback = 反向切流 + 反向搬回 = 用户**二次被踢** + 新数据丢失。框架不假装能自动解决，
把矛盾交给人工（暴露冲突，不折中）。

由此推出：**`RunStatus ∈ {CORE_CUTOVER_DONE, RUNNING_SECONDARY}` 时，`rollback` 命令拒绝执行**，
报错 "already cutover, rollback disabled, use forward-repair"。已切流的 Run 只能"向前修复"
（人工处理 SECONDARY 失败），不能回退。`RUNNING_CORE` 态 rollback 仍允许（未切流，反向搬回 + 反向对账，不踢登录）。

### 7. Kafka 通知两次，payload 加 `phase` 字段

`MigrationNotifier.notify(source, target, payload)` SPI 不变，payload 是 String，
拼上 `phase` 字段区分两次：

- 通知①（CORE 切流后）：`phase=CORE_CUTOVER`，语义"用户登录已切目标区，核心数据可用，次核心还在路上"
- 通知②（SECONDARY 完成后）：`phase=ALL_DONE`，语义"迁移全部完成"

两次通知都向**源区 + 目标区**发送（保持 `KafkaMigrationNotifier` 现有对称语义）。
顺序/去重职责在消费者——Kafka 是 at-least-once，消费者按 `runId + phase` 自处理乱序与重复。
框架不做跨分区顺序保证（不是消息中间件的职责）。

### 8. 单 `migrate` 命令自动驱动两阶段；resume 按 RunStatus 分发

命令行仍是一个 `migrate` 命令，框架内部自动：全批 CORE → 对账 → 踢登录 → 通知① →
全批 SECONDARY → 对账 → 通知② → DONE。**用户不感知 phase 切换。** `dry-run` 演示完整两阶段编排。

`resume --run-id xxx` 不需要新参数，框架按 RunStatus 分发：

| resume 时看到的 status | resume 行为 |
|---|---|
| `RUNNING_CORE` + 有未完成批 | 重做 CORE 批（幂等） |
| `RUNNING_CORE` + 全 DONE 零失败 | 重新对账(CORE) → **重做 evict（幂等兜底）** → 写 `CORE_CUTOVER_DONE` → 通知① |
| `CORE_CUTOVER_DONE` | 转 `RUNNING_SECONDARY`，**不重做 CORE 对账**（信任业务"SECONDARY 不动核心数据"契约） |
| `RUNNING_SECONDARY` + 有未完成批 | 重做 SECONDARY 批 |
| `RUNNING_SECONDARY` + 全 DONE 零失败 | 重新对账(SECONDARY) → 通知② → 写 `DONE` |

第二行的"重做 evict"是 Q4 方案 A 在 resume 上的兑现——evict 幂等使其安全。
第三行的"不重做 CORE 对账"避免无谓开销；契约由 CONTEXT.md Rules "SECONDARY 阶段禁止改写核心数据" 兜底。

## Consequences

- **业务负担上升**：实现 `migrate(..., CORE)` / `migrate(..., SECONDARY)` 两套数据搬迁逻辑 +
  `consistent(..., CORE)` / `consistent(..., SECONDARY)` 两套对账算法 + SECONDARY 的 merge 策略。
  换来的是用户影响窗口的显著缩短。
- **新硬契约：evict 幂等**。与 ADR-0002（migrate 幂等）同性质、同重要性。文档需显著位置强调。
- **状态机从三态变五态**，新增一个对外可见中间态 `CORE_CUTOVER_DONE`。运维需理解。
- **DB schema 变更**：`migration_run` 表新增 `phase VARCHAR(16)` 列。`schema.sql` 是 `CREATE TABLE IF NOT EXISTS`，
  对已存在的表不会自动加列——已部署的环境需手工执行
  `ALTER TABLE migration_run ADD COLUMN phase VARCHAR(16) AFTER status;`。
  本仓库不引入 flyway/liquibase（准则 2 简单优先，schema 管理保持现状手工方式）。
- **SECONDARY 对账失败不可自动恢复**——这是切流不可逆性的必然代价。运维需有"人工修数据"预案。
- **回滚路径收窄**：已切流的 Run 不能 rollback，只能向前修复。设计迁移方案时需评估"次核心对账失败后人工修复"的可行性。
- **窗口期数据正确性是业务职责**——框架不内建读保护。业务需自行设计降级 + merge。
- **status 与 phase 双重编码**：phase 列在 DB 独立存储，但 status 名字（RUNNING_CORE 等）已能编码 phase。
  phase 列在 `createRun` 时一次性写入，后续 `updateRunProgress` 不单独更新 phase（避免扩签名影响所有 store 实现）。
  这是小妥协——若后续出现 status/phase 不同步需求再统一。

## Rejected alternatives

- **租户级滚动切流（CORE 阶段每租户搬完即踢该租户）**：颠覆现有"批是原子单元 / Run 级切流"模型，
  且打开"踢登录后、次核心迁移期间用户重新登录读数据"的死结。复杂度爆炸，业务收益不明。
- **业务声明 N 阶段序列（通用 phase 描述符）**：违反"简单优先"——当前只需 CORE/SECONDARY 二元，
  为"未来可能 N 阶段"提前付出复杂度。从二元枚举演进到描述符是平滑的，无需现在投机。
- **切流 saga 化（预演-确认-补偿）**：严重过度设计。`CutoverAction.evict` 是简单钩子，升级成 saga 等于重写半个框架。
- **SECONDARY 对账失败后自动 rollback**：用户已在目标区、已产生新数据，反向搬回 = 二次灾难。
- **框架内建窗口期读保护（拦截/路由读请求）**：越界。本框架是数据迁移工具，不是应用网关/数据代理。
  读请求路由是应用层/数据访问层职责。
- **通知 payload 升级为结构化对象**：违反"简单优先"。现有 payload 是 k=v 字符串，加一个字段即可，
  结构化对象引入序列化约定（JSON/Avro）与 SPI 签名变更。
- **`CORE_CUTOVER_DONE` 态 resume 重做 CORE 对账**：过度防御。SECONDARY 不动核心数据是契约，
  违反是业务 bug，框架不该为业务 bug 买单。
