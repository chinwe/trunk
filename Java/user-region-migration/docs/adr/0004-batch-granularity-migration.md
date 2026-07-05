# 0004 — 批粒度迁移（业务拿一批）

## Status

accepted (2026-07-05)
supersedes ADR-0003（租户级并发）

## Context

`TenantMigrationTask.migrate` 的签名一直是 `migrate(ctx, List<String> tenantIds, ...)`，
设计文档决策 #6 也写明"租户分片驱动式：框架分租户批，业务自查询自迁移"。
但 ADR-0003 重构并发时，基于当时的现状（引擎实际是单租户调用 `List.of(tenantId)`）
把并发模型改成了"租户级并发"——`batchSize` 仅作进度汇报粒度，不驱动业务调用。

这偏离了 SPI 的本意：业务期望一次拿到一批租户，可以在批内做批量查询/批量写入，
利用中间件的批量 API（如 MySQL `INSERT ... VALUES (...), (...)`、S3 batch put）
大幅提升吞吐。单租户调用让业务丧失批量化优化的空间，也违背决策 #6。

ADR-0003 当时把 `batchSize` 降级为"进度汇报粒度"是错误的方向纠正。

## Decision

回到**批粒度迁移**，并明确三个配套决策：

1. **业务拿一批**：框架按 `batchSize`（`migration.default-batch-size`，默认 50；命令行 `--batch-size`）切批，
   对每批调用 `task.migrate(ctx, batchTenantIds, product, bizLine)`。`batchSize` 可配。

2. **批内全失败**：批 `migrate` 抛异常 → 整批所有租户标 `FAILED` + errorContext。
   批是原子单元（业务在批内要么全做、要么自补偿回滚跨中间件写入）。
   这与"单租户内跨中间件补偿自管"契约一致——补偿的边界由业务界定，
   框架不假设批内可按租户拆分。`MigrationResult` 不扩字段。

3. **批间并发**：`threads`（`migration.default-threads`，默认 4；命令行 `--threads`）个批并行，
   单批内串行（业务一批一个 `migrate` 调用，串行执行）。

4. **限流按批**：每批处理前 acquire 1 个令牌（`rateLimiter.acquire(1)`）。
   限流语义是"控制批调度速率"——每秒最多启动 N 个批。批内中间件的实际访问速率由业务自管
   （业务通过控制批大小、批内并发来间接控制）。框架无法看见批内业务对中间件的访问次数，
   故不预设按租户/按访问次数限流。

## Consequences

- 失败定位粒度=批（默认 50 个租户）。批内 1 个坏租户会让同批其余也标 FAILED，
  即使业务实际已迁移成功。但失败后 `resume` 仍可重做整批（依赖业务 migrate 幂等，ADR-0002），
  且运维介入的粒度本来就是批。若需更细的失败定位，业务可在批内 catch 单租户异常、
  把成功租户的数据持久化、再重新抛出（业务自管，框架不强制）。
- 批是状态翻转的最小单元：批内所有租户统一 PENDING → RUNNING → DONE/FAILED。
  这意味着"孤儿恢复"的粒度也是批（见 CONTEXT.md）。
- `batchSize` 与 `threads` 解耦：`batchSize` = 业务调用粒度 + 状态原子单元；
  `threads` = 批间并发度。
- 业务可利用批做批量优化（批量查询、批量 UPSERT），吞吐显著高于单租户调用。

## Rejected alternatives

- **租户级并发（ADR-0003）**：基于"业务拿单租户"的错误前提。撤销。
- **业务自报结构化结果（per-tenant status）**：失败定位精确到租户，但需扩 `MigrationResult`
  为结构化列表、业务负担重，且违背"批是补偿单元"的简洁性。不采纳。
