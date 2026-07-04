# 0003 — 租户级并发，撤销批次内串行

## Status

accepted (2026-07-05)
supersedes design-spec 决策 #11（批次间并发、单批内串行）

## Context

初版 `TenantBatcher` 把租户切成 N 个 batch，每 batch 一个 Future，单 batch 内串行处理。设计文档决策 #11 解释"批次间并发、单批内串行"是为"吞吐与安全平衡"。

实际负载严重不均：51 租户、batch=50、threads=4 时只产生 2 个 batch → 只有 2 个线程在跑，第一批串行处理 50 个租户非常久，**threads=4 形同虚设**。"单批内串行"是为安全的过时假设——单租户已有 `migrateSingleTenant` 的 try-catch 隔离，租户级并发天然安全。

## Decision

改为**租户级并发**：每个租户一个 `CompletableFuture`，提交到固定大小（`threads`）线程池，pool 自然并发 `threads` 个租户。`batch` 概念降级为进度汇报粒度（每完成 N 个打一次日志），不再作为并发粒度。

## Consequences

- `threads` 真正生效：N 个租户在 `threads` 个线程上均衡并行。
- `TenantBatcher.partition` 不再驱动并发，仅保留用于进度汇报的分批切片。
- "batch" 与 "threads" 解耦，配置语义清晰：`batch-size` = 进度汇报周期，`threads` = 实际并发度。
