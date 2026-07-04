# user-region-migration

一个用户区域迁移**框架**（非端到端业务实现）：为 SaaS 服务的区域拆分提供多数据源通用迁移、可靠性、回滚与状态维护能力。各业务在本框架内自行实现核心数据迁移逻辑。

## Language

### 迁移执行

**Run**:
一次迁移执行的完整记录，对应 `migration_run` 表一行。由 `runId` 唯一标识，承载方向、源/目标 region、状态与计数。
_Avoid_: job, task execution, batch

**Direction**:
Run 的方向。`FORWARD` = 正向迁移（如新加坡→缅甸）；`ROLLBACK` = 逆向回滚（如缅甸→新加坡）。两者复用同一个业务 `migrate` 方法，仅 source/target 对调。
_Avoid_: mode, flow

**Tenant State**:
单个租户在某次 Run 中的处理状态（`PENDING` / `RUNNING` / `DONE` / `FAILED`），对应 `migration_tenant_state` 表。租户级断点的载体。
_Avoid_: checkpoint, progress entry

**Orphan Tenant**:
崩溃后卡在 `RUNNING` 中间态、既非 `PENDING` 也非 `DONE` 的租户。`resume` 命令将其视同 `PENDING` 重做（依赖业务 migrate 幂等）。
_Avoid_: stuck tenant, zombie

### 业务接缝

**Migration Task**:
业务插件实现的 `TenantMigrationTask` SPI，提供方向无关的 `migrate` 方法。框架按租户分批后驱动。**契约要求 migrate 必须幂等**——同一租户被调用 N 次与 1 次结果等价。
_Avoid_: job, worker

**Cutover**:
迁移完成后将流量从源区切到目标区的不可逆动作（踢登录、清会话、发通知）。框架仅在数据一致性确认后触发。
_Avoid_: switch, flip, promote

**Reconciliation**:
切流前对源/目标数据一致性的校验。**算法由业务自证**（`ReconciliationChecker` SPI）：业务可用 count 差、checksum、抽样或任意方式证明一致，框架只接受二值判定。
_Avoid_: verification（保留给 `verify` 命令的深度校验）, audit

### 验证与对账的区分

**Reconciliation vs Verify**:
两者不同。**Reconciliation** 是切流前框架调用的闸门（`ReconciliationChecker`），轻量、必须通过才允许切流。**Verify** 是独立的 `verify` 命令（`TenantMigrationTask.verify` 钩子），深度校验，可任意时机手动触发，不阻塞切流。

## Rules

- **方向无关**：业务只实现一个 `migrate`，禁止硬编码 region。回滚 = source/target 对调复用同一逻辑。
- **真搬迁**：业务 `migrate` 默认语义是从源读→写目标→删源（删源区）。回滚时框架对调 region，同一逻辑天然反向。
- **单租户隔离**：单个租户失败不影响整批其余租户，失败租户记 `FAILED` + errorContext。
- **幂等是硬约束**：业务 `migrate` 必须幂等。这是"可恢复/可逆"承诺的数学前提——resume/retry/rollback 都会重做同一租户。
- **零失败切流**：Run 中有任意 `FAILED` 租户时不允许切流，Run 标 `FAILED` 等待人工处理或 resume。
