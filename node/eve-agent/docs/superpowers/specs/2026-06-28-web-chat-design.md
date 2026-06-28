# Web Chat 功能设计

> 状态：已与用户确认方案，待 spec review
> 日期：2026-06-28
> 作者：chinwe + Claude

## 1. 概述

为 eve-agent（**eve 框架学习助手**）增加一个网页端聊天界面，供**本地开发与自测**使用。用户在浏览器中可与 agent 多轮对话，并**完整展示 agent 的中间过程**：

- 助手文本回复（流式逐字呈现）；
- 每次工具调用（`list_eve_docs` / `read_eve_doc`）的名称、入参与返回结果；
- 思考过程（reasoning）。

## 2. 目标与成功标准

- [ ] 在浏览器中打开页面即可与 eve-agent 聊天。
- [ ] 助手回复以流式方式逐字呈现。
- [ ] 每次 tool call 显示为卡片：工具名、入参、返回结果。
- [ ] 思考（reasoning）以可区分的样式呈现。
- [ ] 支持多轮续发（基于 `continuationToken` 复用同一 durable session）。
- [ ] 错误（`step.failed` / `turn.failed` / `session.failed`）在界面内可见。
- [ ] 不引入新的运行时依赖或前端框架；`npm run typecheck` 通过。

## 3. 非目标（YAGNI）

- 生产部署、多用户、真实 auth —— 仅依赖 `localDev()` loopback 放行。
- 会话历史持久化到数据库。`localStorage` 会话恢复为可选增强，不阻塞 MVP。
- HITL（`input.requested`）审批 UI —— 当前 agent 工具无 approval 需求。
- 文件上传、多会话切换、reasoning 隐私开关。

以上均可在 MVP 之后按需扩展。

## 4. 背景与约束

- 项目为纯 eve agent（ESM / TypeScript, Node 24），**无前端框架**。
- eve 每个 app 内置稳定 HTTP API（见 `node_modules/eve/docs/concepts/sessions-runs-and-streaming.md`）：
  - `POST /eve/v1/session` —— 发首条消息，响应体含 `{ sessionId, continuationToken }`，响应头 `x-eve-session-id`。
  - `GET /eve/v1/session/<sessionId>/stream` —— NDJSON 事件流（`application/x-ndjson`），支持 `?startIndex=` 重连。
  - `POST /eve/v1/session/<sessionId>` —— 携带 `continuationToken` 续发后续消息。
- 现有 `agent/channels/eve.ts`（内置 HTTP channel）提供上述路由与 auth 链（`vercelOidc()` + `localDev()` + `placeholderAuth()`）。本设计**保留其不动**。
- 自定义 channel（`agent/channels/*.ts`，`defineChannel`）可定义任意 HTTP/WS 路由并返回任意 `Response`（见 `node_modules/eve/docs/channels/custom.mdx`），用于托管网页。

## 5. 方案选型

| 方案 | 做法 | 优点 | 代价 |
|---|---|---|---|
| **① 极简静态页 + 自定义 channel（选定）** | 新增 custom channel 托管单页 HTML；前端 vanilla JS 直连同源 `/eve/v1/*` | 零新依赖、零构建链；同源无 CORS；不改项目结构 | 需手写 NDJSON 事件解析与渲染，一次性工作量 |
| ② React + `useEveAgent` | 引入 React + Vite，用官方 hook | 事件投影省事 | 引入前端框架与构建链；跨端口需 CORS/host |
| ③ 官方 `--channel-web-nextjs` | eve 内置 Next.js Web Chat | 最官方、开箱即用 | 改造成 Next.js 项目，结构变动大 |

**选定方案 ①**：本地自测场景下，引入 React/Next.js 属投机性复杂度；vanilla JS 直连 eve 已有 HTTP API 最直接，且能精确控制工具调用卡片与思考块的展示。

## 6. 架构

```
浏览器 (单页 HTML)                eve dev server (同源, 默认 :3000)
┌────────────────────┐           ┌──────────────────────────────────┐
│  渲染区 / 输入区    │           │ agent/channels/eve.ts (不动)      │
│  会话游标(内存)     │  fetch──► │   /eve/v1/session                │
│  NDJSON 流读取      │  fetch──► │   /eve/v1/session/<id>/stream    │
└────────────────────┘           │   /eve/v1/session/<id>           │
        ▲                        │ agent/channels/web-chat.ts (新增) │
        │ HTML                   │   GET 路由 → 返回单页 HTML         │
        └────────────────────────┘                       │
                                                           │
                         agent/lib/web-chat-page.ts (新增) │
                           导出 WEB_CHAT_HTML 字符串 ◄──────┘
```

- **新增 `agent/channels/web-chat.ts`**：custom channel，定义一个 `GET` 路由，返回单页 HTML。
- **新增 `agent/lib/web-chat-page.ts`**：导出 `WEB_CHAT_HTML` 常量（含 HTML + CSS + JS）。`lib/` 是 import-only 源码（不进 workspace），把页面内容与 channel 路由逻辑分离，且避免运行时 `fs` 读取在 dev / build 下路径不一致的风险。
- 前端 JS 以**绝对路径**调用同源 `/eve/v1/*`，与 channel 自身的挂载前缀解耦。
- HTML 路由本身无需鉴权（仅返回静态页面，不含敏感数据）；浏览器调用的 `/eve/v1/*` 路由受现有 eve channel 的 `localDev()` 保护，loopback 请求即放行。

## 7. 组件

### 7.1 `agent/channels/web-chat.ts`（custom channel）

- `defineChannel({ routes: [ GET("<path>", handler) ] })`。
- handler 返回 `new Response(WEB_CHAT_HTML, { headers: { "content-type": "text/html; charset=utf-8" } })`；`WEB_CHAT_HTML` 通过 `import { WEB_CHAT_HTML } from "#lib/web-chat-page.js"` 引入（复用 `package.json` 既有的 `#*` → `./agent/*` 导入映射）。
- 无需 `events`、无需 CORS（同源）。
- `<path>` 与最终浏览器访问 URL 的前缀在实现时用 `eve info` 确认（见 §11）。

### 7.2 前端单页（`agent/lib/web-chat-page.ts` 内的 HTML 字符串）

**状态**：`sessionId`、`continuationToken`、`streamIndex`、`status`（`ready` / `streaming` / `error`），存于内存。

**核心函数**：
- `sendMessage(text)` —— 首条 `POST /eve/v1/session`；续发 `POST /eve/v1/session/<id>`（带 `continuationToken`）。成功后 `openStream()`。
- `openStream()` —— `GET /eve/v1/session/<id>/stream`，用 `ReadableStream` reader 逐行读取 NDJSON，逐行 `JSON.parse` 后交给 `handleEvent(event)`。
- `handleEvent(event)` —— 按 `event.type` 路由到渲染函数。
- 渲染：用户气泡、助手气泡（流式追加）、思考块、工具调用卡片、错误条。
- 输入区：文本框 + 发送按钮，按 `status` 禁用 / 启用；回车发送。

## 8. 数据流（事件处理）

依据 eve 事件契约（`sessions-runs-and-streaming.md`）：

| 事件 | 前端处理 |
|---|---|
| `session.started` | 记录 `sessionId` |
| `turn.started` | `status = streaming`，禁用输入 |
| `message.appended` | 助手文本**增量**追加到当前助手气泡（事件带 delta 与 cumulative） |
| `message.completed` | 助手文本定稿；按 `data.finishReason` 判断是否终态回复（一轮内可能多次触发） |
| `reasoning.appended` | 思考增量追加到思考块 |
| `reasoning.completed` | 思考块定稿 |
| `actions.requested` | 新建工具调用卡片：工具名 + 入参（calls 在执行前即流式到达） |
| `action.result` | 把返回结果填入对应工具卡片 |
| `step.completed` | 步骤结束（含 usage，可选不展示） |
| `step.failed` / `turn.failed` / `session.failed` | 在错误条显示 `{ code, message }` |
| `session.waiting` | `status = ready`，恢复输入（可续发下一条） |
| `session.completed` | 会话终态 |

> 说明：`message.completed` 在一轮内可能多次触发（tool call 前的中间叙述）。仅当 `finishReason` 表明是终态回复时才视作本轮最终答案；中间叙述也照常渲染。

## 9. 错误处理

- `fetch` 失败 / 网络错误 → 错误条提示，允许重试。
- `step.failed` / `turn.failed` / `session.failed` → 展示事件载荷中的 `{ code, message }`。
- 流中断重连：MVP 先简化为「提示重试」；按 `startIndex` 重连列为可选增强（非阻塞）。

## 10. 测试与验证

- **类型检查**：`npm run typecheck` 通过。
- **端到端手动验证**（`eve dev --no-ui` 起服务，浏览器打开页面）：
  1. 发「列出所有 eve 文档」→ 看到 `list_eve_docs` 工具卡片（名称 + 参数 + 结果）+ 助手流式回复。
  2. 续发「读 getting-started 文档」→ 看到 `read_eve_doc` 工具卡片 + 回复，且复用同一会话历史。
  3. 断网 / 中止 → 看到错误条提示。
- **可选冒烟脚本**：用 `eve/client` SDK 创建 session 并读流，独立验证后端（不依赖 UI）。

## 11. 待实现时确认的点（不影响方案成立）

- **custom channel 路由的 URL 前缀**：用 `eve info` 查看，决定浏览器访问页面的地址。前端调 API 用绝对路径 `/eve/v1/*`，不受该前缀影响。
- **reasoning 事件是否实际产出**：取决于当前模型（智谱 GLM）。若不产出，思考块区域自然为空，不影响功能。

## 12. 实现产物清单

- 新增 `agent/lib/web-chat-page.ts`
- 新增 `agent/channels/web-chat.ts`
- 不改动：`agent/agent.ts`、`agent/channels/eve.ts`、`agent/instructions.md`、`package.json`（零新依赖）
