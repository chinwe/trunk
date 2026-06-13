# kratos-demo

基于 [go-kratos v2](https://go-kratos.dev/) 的 Hello World 微服务示例，用于学习和演示 Kratos 的核心概念与标准分层架构。

## 架构分层

本项目遵循 Kratos 标准分层，请求流向为 `server → service → biz → data`：

| 目录 | 职责 |
|------|------|
| `api/helloworld/v1/*.proto` | 接口定义。修改 `.proto` 后必须执行 `make api` 重新生成 |
| `internal/biz/` | 业务用例层。定义 `Repo` 接口与 `Usecase`，核心业务逻辑放这里 |
| `internal/service/` | 服务层。实现 proto 生成的 Service，将请求转发给 biz usecase |
| `internal/data/` | 数据层。实现 biz 定义的 `Repo` 接口，含 wire `ProviderSet` |
| `internal/server/` | HTTP/gRPC server 装配、Prometheus 指标暴露（`Metrics` 类型） |
| `internal/conf/` | 配置 proto（`config.yaml` 反序列化为结构） |
| `cmd/demo/` | 程序入口 + wire 依赖注入（`wire.go` / `wire_gen.go`） |

依赖方向：`biz` 定义接口，`data` 实现它（依赖倒置）。修改 ProviderSet 或依赖关系后必须执行 `make wire`。

## 常用命令

| 命令 | 作用 |
|------|------|
| `make build` | 编译到 `./bin/` |
| `make init` | 首次拉取并整理依赖（`go mod download` + `go mod tidy`） |
| `make run` | 运行服务（`go run ./cmd/demo/... -conf ./configs`） |
| `make test` | 运行全部测试 |
| `make api` | buf 生成 proto 代码（改 `.proto` 后执行） |
| `make wire` | 生成依赖注入代码（改 `ProviderSet` 后执行） |
| `make lint` | buf proto lint 检查 |
| `make breaking` | proto 破坏性变更检测（对比 master 分支） |

服务端口：HTTP `:8000`、gRPC `:9000`（见 `configs/config.yaml`）。

> **无 `make` 环境**：若未安装 `make`（如 Windows Git Bash），直接用底层命令替代：
> `go build ./cmd/demo/...` · `go test ./...` · `go run ./cmd/demo/... -conf ./configs` · `buf generate` · `cd cmd/demo && wire` · `buf lint` · `buf breaking --against '.git#branch=master'`

> **容器化**：项目含多阶段 `Dockerfile`（`EXPOSE 8000 9000`），用 `docker build -t kratos-demo .` 构建。

## 验证清单（完成工作前必须确认）

"完成"的标准是**能跑起来**，而不只是编译/测试通过。每次改动后按顺序确认：

1. `make build` — 编译通过
2. `make test` — 全部测试通过
3. `make run` — 服务正常启动（监听 8000/9000）
4. `curl http://localhost:8000/helloworld/kratos` — 返回 `{"message":"Hello kratos"}`
5. （可选）`curl http://localhost:8000/metrics` — 返回 Prometheus 文本格式指标
6. 若改了 proto：`make lint && make breaking` — 无 lint 错误、无破坏性变更

## 代码规范

- **代码（含日志、字符串字面量）一律用英文**，例如 `s.log.Infof("SayHello request: name=%s", req.Name)`
- **注释一律用中文**，例如 `// GreeterRepo 定义数据访问接口`
- proto 文件中的 service/message 注释用中文

## 生成代码说明

以下文件由工具生成，**不要手动编辑**：

- `api/helloworld/v1/*.pb.go`、`*_grpc.pb.go`、`*_http.pb.go` — 由 `make api` 从 `.proto` 生成
- `cmd/demo/wire_gen.go` — 由 `make wire` 从 `wire.go` 生成
- `internal/conf/conf.pb.go` — 由 `make api` 生成

## 可观测性（Metrics）

服务经 `internal/server/metrics.go` 的 `Metrics` 类型暴露 Prometheus 指标：

- 请求计数与处理延迟直方图由 kratos `metrics` 中间件采集，HTTP/gRPC 共用同一实例，以 `kind` 标签区分 transport。
- `/metrics` 端点直挂 HTTP 业务端口（`:8000`）的底层 mux，**绕过** recovery/tracing/logging 中间件链，故抓取不产生访问日志与 trace span。
- 使用独立 Prometheus registry（非全局默认），多实例/测试间互不干扰；另注册 Go runtime 与 process 指标。
- 零配置（`config.yaml` 无需 metrics 段），无 proto 改动。
- `server.ProviderSet` 含 `NewMetrics`，改它后须执行 `make wire`。

## Agent 工作流

本项目在 `.claude/` 下内置以下 agent 基础设施：

- **code-reviewer subagent**（`.claude/agents/code-reviewer.md`）— 独立、只读的代码审查者。实现功能或修复后、提交前可委派给它，审查改动是否符合分层约定与代码规范。它与写代码的 agent 分离（独立 context、无 `Edit`/`Write` 权限），不信任实现者的自评。
- **pre-commit hook**（`.claude/hooks/pre-commit-test.py`）— 每次 `git commit` 前自动运行 `go test ./...`，测试失败则阻止提交。通过 `.claude/settings.local.json` 注册（个人本地配置，不进版本库）。

> 注：subagent 与 hook 在会话启动时加载，新增后需新开会话或经 `/agents`、`/hooks` 菜单确认才生效。

## 相关文档

- `CONTEXT.md` — 领域词汇表（Greeter、问候消息等术语定义）
- `AGENTS.md` — agent 配置（issue tracker、triage、domain 文档位置）
- `docs/adr/` — 架构决策记录
