# 0001 — 对账由业务自证一致性

## Status

accepted (2026-07-05)

## Context

初版用 `ReconciliationCounter` SPI：业务提供 `count(region, run)`，框架对比源/目标 count 是否相等。但业务 `migrate` 是**真搬迁（删源）**语义——被搬的数据在源区已删，迁移后源 count ≈ 0、目标 count = N，**永远不相等**。

要让它"通过"，业务只能统计不受搬迁影响的参照物（如 tenant 表），这时闸门只能证明"租户还在"，无法证明"数据搬对了"——R2 灾难风险（搬错数据即切流）实质未解。

## Decision

将 SPI 改为 `ReconciliationChecker`，签名 `boolean consistent(MigrationRun run, List<String> migratedTenantIds)`。**对账算法全权由业务决定**：可用 count 差、checksum、抽样、逐条比对或任意组合。框架只接受二值判定，不通过则不切流。

## Consequences

- 业务实现负担上升：必须自行设计一致性证明。
- 框架不再假设对账是"廉价 COUNT"——可能耗时较久，运维预期需调整。
- `AlwaysPassReconciliationGate` 仍作为兜底（无 Checker Bean 时默认通过），保持向后兼容。

## Rejected alternatives

- **快照 + 搬迁后比对**：框架在 migrate 前记源 count=N，迁移后校验 `target==N && source==0`。开箱即用，但限定 COUNT 级，无法表达 checksum/抽样等更强校验，且需迁移前额外扫源。
- **保留 count + 收紧契约**：维持 `CountReconciliationGate`，要求 counter 统计"不受搬迁影响的参照物"。改动最小，但闸门形同虚设，R2 风险未解。
