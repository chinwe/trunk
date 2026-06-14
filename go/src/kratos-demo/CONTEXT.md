# kratos-demo — 领域词汇表

## 项目

一个基于 go-kratos v2 框架的 Hello World 微服务示例，用于学习和演示 Kratos 的核心概念与标准分层架构。每次问候请求会被持久化为一条问候记录（落 MySQL）。

## 领域词汇

| 术语 | 定义 |
|---|---|
| **Greeter（问候者）** | 本服务的核心实体。对外提供 SayHello 问候能力。 |
| **问候消息** | Greeter 处理请求后返回的文本消息，格式为 `Hello {name}`。 |
| **问候请求** | 客户端发起的一次 SayHello 调用，包含一个 `name` 参数。 |
| **问候响应** | 服务端返回的问候消息，包含一个 `message` 字段。 |
| **问候记录** | 每次问候请求落库的一条持久化记录，含 `name`、生成的 `message`、`created_at`。 |

## 概念关系

```
客户端 ──→ Greeter 服务 ──→ 问候消息
           │
           ├── 接收问候请求（含 name）
           ├── 生成问候消息（Hello {name}）
           ├── 持久化问候记录（落 MySQL）
           └── 返回问候响应（含 message）
```

## 术语边界

- **name** — 被问候的对象名称，字符串类型，允许空字符串。
- **message** — 生成的问候文本，格式固定为 `Hello {name}`。
- **问候记录** — 持久化于 MySQL 的 `greetings` 表，由 data 层在每次 `CreateHello` 时写入一条。
