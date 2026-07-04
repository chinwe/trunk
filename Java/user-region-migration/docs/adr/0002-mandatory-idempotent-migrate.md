# 0002 — 业务 migrate 必须幂等

## Status

accepted (2026-07-05)

## Context

`resume`、`RetryStrategy`、`rollback` 都会对同一租户重复调用 `task.migrate`：
- `resume` 重做 PENDING（以及修复后的 RUNNING 孤儿，见 ADR-0003 上下文）；
- `RetryStrategy` 在瞬时失败后重试同一租户；
- `rollback` 对原 DONE 租户反向再调一次 migrate。

但初版 SPI 契约从未声明幂等要求，示例代码用非幂等的 `INSERT`，给业务示范了错误模式。在真实搬迁中，一次 retry 或 resume 即可能产生重复数据。

## Decision

在 `TenantMigrationTask.migrate` 的 Javadoc 顶部以**契约**级别声明：本方法必须幂等——同一租户被调用 N 次与 1 次的结果等价。建议用 `INSERT ... ON DUPLICATE KEY UPDATE` / `INSERT IGNORE` / 先删后插 / UPSERT 等模式。示例 `UserMigrationTask` 同步改为幂等实现。

## Consequences

- 业务实现约束增加：必须为每个中间件写入设计幂等策略（部分中间件如 S3 天然按 key 幂等，MySQL/DynamoDB 需显式设计）。
- 这是"可恢复/可逆"承诺的数学前提——没有幂等，断点续传与回滚都不安全。
- 文档需在业务插件开发指南显著位置强调此约束。

## Rejected alternatives

- **软建议**：仅在 README 提一句"建议幂等"。负担最轻，但业务仍会踩非幂等 INSERT 的坑，框架无法保证恢复语义。
