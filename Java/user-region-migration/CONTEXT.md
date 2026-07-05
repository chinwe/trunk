# user-region-migration

一个用户区域迁移**框架**（非端到端业务实现）：为 SaaS 服务的区域拆分提供多数据源通用迁移、可靠性、回滚与状态维护能力。各业务在本框架内自行实现核心数据迁移逻辑。

## Language

### 迁移执行

**Run**:
一次迁移执行的完整记录，对应 `migration_run` 表一行。由 `runId` 唯一标识，承载方向、源/目标 region、状态、阶段与计数。
_Avoid_: job, task execution

**Migration Phase**:
迁移的阶段标识（`CORE` / `SECONDARY`），贯穿 `migrate` 与 `consistent` 两个 SPI 参数。**核心**=用户登录后立即必需的数据（如基本信息、订单、账户余额）；**次核心**=短时缺失可容忍/可降级的数据（如历史行为、收藏、通知记录）。框架硬编码"全 CORE 完成后切流、再全 SECONDARY"的编排顺序，不暴露 phase 序列配置。
_Avoid_: stage, round, wave, tier

**Direction**:
Run 的方向。`FORWARD` = 正向迁移（如新加坡→缅甸）；`ROLLBACK` = 逆向回滚（如缅甸→新加坡）。两者复用同一个业务 `migrate` 方法，仅 source/target 对调。
_Avoid_: mode, flow

**Batch**:
框架按 `batchSize`（`migration.default-batch-size`，默认 50；命令行 `--batch-size`）切出的租户组，是**业务调用与状态原子单元**。每批一次 `task.migrate(ctx, batchTenantIds, ...)`；批内所有租户统一翻转为 DONE 或 FAILED（ADR-0004）。批间并发由 `threads` 控制。
_Avoid_: chunk, partition

**Tenant State**:
单个租户在某次 Run 中的处理状态（`PENDING` / `RUNNING` / `DONE` / `FAILED`），对应 `migration_tenant_state` 表。状态翻转以批为单位——批内所有租户同时进入同一状态。
_Avoid_: checkpoint, progress entry

**Run Phase State（两阶段中间态，ADR-0005）**:
Run 级状态机扩展为五态：`RUNNING_CORE → CORE_CUTOVER_DONE → RUNNING_SECONDARY → DONE`（任何阶段失败 → `FAILED`）。`CORE_CUTOVER_DONE` 是**对外可见的中间态**——核心已对账、已踢登录、已发通知①，次核心尚未开始。运维需理解此中间态。
_Avoid_: step, stage

**Orphan Batch**:
崩溃后卡在 `RUNNING` 中间态的批（批内租户既非 `PENDING` 也非 `DONE`）。`resume` 命令将其视同 `PENDING` 重做（依赖业务 migrate 幂等）。
_Avoid_: stuck batch, zombie batch

### 业务接缝

**Migration Task**:
业务插件实现的 `TenantMigrationTask` SPI，提供方向无关的 `migrate(ctx, List<String> tenantIds, ...)` 方法。框架按 `batchSize` 切批后驱动，业务每批一次调用，可在批内做批量查询/批量写入。**契约要求 migrate 必须幂等**——同一批租户被调用 N 次与 1 次结果等价。
_Avoid_: job, worker

**Cutover**:
迁移完成后将流量从源区切到目标区的不可逆动作（踢登录、清会话、发通知）。框架仅在数据一致性确认后触发。两阶段下（ADR-0005）切流发生在 CORE 阶段完成后——核心数据搬完 + 对账通过即踢全部登录，**不等次核心**。
_Avoid_: switch, flip, promote

**Core Cutover（核心切流，ADR-0005）**:
两阶段迁移的第一道切流——全部租户的核心数据搬完 + CORE 对账通过后，一次性踢全部登录 + 发通知①（`phase=CORE_CUTOVER`）。切流后进入窗口期（见下）。
_Avoid_: pre-cutover, soft cutover

**Reconciliation**:
切流前对源/目标数据一致性的校验。**算法由业务自证**（`ReconciliationChecker` SPI）：业务可用 count 差、checksum、抽样或任意方式证明一致，框架只接受二值判定。两阶段下（ADR-0005）按 `MigrationPhase` 分两次：CORE 对账是切流闸门（不通过不切流），SECONDARY 对账是收尾闸门（不通过 Run 标 FAILED 等人工，**不自动 rollback**）。
_Avoid_: verification（保留给 `verify` 命令的深度校验）, audit

**Cutover Window（窗口期，ADR-0005）**:
核心切流（`CORE_CUTOVER_DONE`）到 SECONDARY 全部完成之间的时间段。期间用户已在目标区读写，同时框架还在从源区搬次核心数据。**窗口期的读缺失与写冲突职责全部在业务**：读缺失由应用层降级，写冲突由 SECONDARY migrate 实现 merge（推荐 UPSERT + `updated_at`）。框架不感知数据语义、不内建读保护、不假设踢登录后源区冻结。
_Avoid_: gap, in-between

### 验证与对账的区分

**Reconciliation vs Verify**:
两者不同。**Reconciliation** 是切流前框架调用的闸门（`ReconciliationChecker`），轻量、必须通过才允许切流。**Verify** 是独立的 `verify` 命令（`TenantMigrationTask.verify` 钩子），深度校验，可任意时机手动触发，不阻塞切流。

## Rules

- **方向无关**：业务只实现一个 `migrate`，禁止硬编码 region。回滚 = source/target 对调复用同一逻辑。
- **真搬迁**：业务 `migrate` 默认语义是从源读→写目标→删源（删源区）。回滚时框架对调 region，同一逻辑天然反向。
- **批是原子单元**：业务一次拿一批（batchSize 个租户）。批 `migrate` 抛异常 → 整批所有租户标 `FAILED`（批级隔离）。批内成功/失败混合的精细化定位由业务在 migrate 内自管（catch 单租户异常、持久化成功者、再决定是否重新抛出）。
- **幂等是硬约束**：业务 `migrate` 必须幂等。这是"可恢复/可逆"承诺的数学前提——resume/retry/rollback 都会重做同一批。
- **零失败切流**：Run 中有任意 `FAILED` 租户时不允许切流，Run 标 `FAILED` 等待人工处理或 resume。

### 两阶段迁移（ADR-0005）

- **两阶段编排由框架硬编码**：全 CORE 完成 → 对账(CORE) → 切流 → 通知① → 全 SECONDARY → 对账(SECONDARY) → 通知② → DONE。业务不感知 phase 切换，命令行只暴露一个 `migrate`。
- **核心/次核心分类由业务决定**：业务在 `migrate(..., phase)` / `consistent(..., phase)` 内部按 phase 分流数据集。框架不定义哪些数据算核心。
- **evict 必须幂等（新硬契约）**：`CutoverAction.evict` 对同一批租户调用 N 次与 1 次结果等价。两阶段下 CORE 切流与 SECONDARY 迁移之间可达数小时，崩溃重做 evict 是常态而非边缘，幂等是安全基石。
- **SECONDARY 阶段禁止改写核心数据**：业务必须保证 SECONDARY migrate 不动 CORE 已搬好的核心数据，否则 `CORE_CUTOVER_DONE` 态 resume 跳过 CORE 对账的前提失效。
- **窗口期职责在业务**：读缺失由应用层降级，写冲突由 SECONDARY migrate 实现 merge。框架不内建读保护、不假设源区冻结、不依赖"低峰期"运维安排。
- **已切流态禁 rollback**：`RunStatus ∈ {CORE_CUTOVER_DONE, RUNNING_SECONDARY}` 时 `rollback` 拒绝执行（已切流不可逆，反向切流会二次踢用户 + 丢新数据）。SECONDARY 对账失败 → Run 标 FAILED 等人工向前修复。
