# 0001: 问候记录持久化 — ent + MySQL + 环境变量 DSN

- **Status**: accepted
- **Date**: 2026-06-14

## Context

kratos-demo 原为无状态 Hello World 示例，`CONTEXT.md` 明确「无持久化需求，问候消息不存储、不缓存」。架构审视发现：

- `GreeterRepo` 接口名为「数据访问」，实现却只是 `fmt.Sprintf`；
- `GreeterUsecase.SayHello` 是纯直通器（`return repo.CreateHello(ctx, name)`）；
- 整条问候管线穿过 `service → biz → data` 四层只为格式化字符串，两个 shallow module 堆叠，名为 Repo 实为纯函数。

为让「Repo」名副其实、让分层有真实承载对象，决定引入真实持久化：每次问候请求落库一条问候记录。

## Decision

问候记录经 **ent**（entgo.io）持久化到本地 **MySQL**：

- ent schema 定义 `Greeting`（`name` / `message` / `created_at`），位于 `internal/data/ent/schema/`，改 schema 后执行 `go generate ./internal/data/ent/` 重新生成。
- `internal/data/data.go` 的 `Data` 持有 ent 客户端；`NewData` 启动时 `client.Schema.Create` 自动建表（幂等）。
- `greeterRepo.CreateHello` 生成 `Hello {name}` 后经 ent `INSERT` 落库。
- **DSN 经环境变量 `DATABASE_SOURCE` 注入**：`configs/config.yaml` 只放 `${DATABASE_SOURCE}` 占位，`NewData` 用 `os.ExpandEnv` 展开。明文密码不入库、不入配置。开发时用 `.env`（`godotenv` 加载到环境变量，`.gitignore` 已排除，模板 `.env.example` 入库）；生产可改用真实环境变量或 secret manager。

## Alternatives considered

**ORM 选型**

- **`database/sql` + `go-sql-driver/mysql`**：最轻、无代码生成、`go.mod` 零 ORM 依赖，延续 demo「无框架魔法」风格。**未选**：用户选择跟随 Kratos 官方脚手架（`kratos new` 生成的 layout 默认用 ent）的惯例，换取与框架生态的一致性。对当前 demo 体量其实 `database/sql` 更合适，但一致性优先。
- **GORM**：生态大但非 Kratos 官方推荐，反射魔法更重，类型安全与代码生成不如 ent。未选。
- **不持久化（维持现状）**：保留纯 `fmt.Sprintf`。**未选**：与本次目的（让 Repo 名副其实）相悖，且架构审视已识别为应消解的摩擦。

**密码方案**

- 明文 DSN 写 `configs/config.yaml`：简单但密码入库，不安全。
- 真实环境变量（无 `.env`）：符合 12-factor，但开发时每次开终端都要重设。
- **环境变量 `DATABASE_SOURCE` + `.env` 开发便利（选中）**：`godotenv` 在 `main` 启动时加载 `.env`（已 gitignore）到环境变量，统一走 `os.ExpandEnv`；配置文件可安全入库，开发免重复设置，生产去掉 `.env` 即回退到真实环境变量。

## Consequences

- **正向**：`GreeterRepo` 名副其实，问候管线不再是空壳直通；ent 提供类型安全的查询 API；schema 变更经代码生成纳入类型检查。
- **代价**：
  - 引入 ent + 生成代码（`internal/data/ent/**`，单次提交 34 文件），`go.mod` 依赖增多；改 schema 须 `go generate`。
  - `make run` 现依赖本地 MySQL 可达（`configs/init.sql` 建库；表由 ent 自动建），不再是「零外部依赖」demo。
  - 测试需 mock SQL：data 层用 `sqlmock`（无 CGO，满足 pre-commit hook 在无 gcc 环境下跑 `go test`）；若用 `enttest + sqlite` 则需 CGO，本机 `CGO_ENABLED=0` 不可行。
- **不可逆性**：移除持久化需删 `data.go` + `ent/**` + `conf.proto` 的 `Data` 段 + wire 改回，工作量中等。

## 相关

- `CONTEXT.md`：领域词汇表已新增「问候记录」术语，推翻「无持久化需求」。
- `CLAUDE.md`「数据层（MySQL / ent）」段：实现细节与启动步骤。
