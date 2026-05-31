# kratos-demo 微服务设计文档

## 概述

基于 go-kratos v2 框架搭建一个 Hello World 微服务示例，用于学习框架核心概念和标准项目结构。采用 kratos CLI 脚手架生成标准分层架构。

## 目标

- 理解 go-kratos 的项目结构和分层架构
- 实现 HTTP + gRPC 双协议的 Greeter 服务
- 掌握 Wire 依赖注入、中间件、配置管理等核心功能

## 技术栈

| 组件 | 选型 |
|------|------|
| 框架 | go-kratos v2 |
| API 定义 | Protocol Buffers 3 |
| 依赖注入 | Google Wire |
| 传输协议 | HTTP + gRPC |
| 日志 | kratos/log（结构化日志） |

## 项目结构

```
kratos-demo/
├── api/helloworld/v1/       # Proto 定义及生成代码
├── cmd/demo/                # 程序入口 + Wire
├── configs/                 # YAML 配置
├── internal/
│   ├── conf/                # 配置结构体 (proto)
│   ├── server/              # HTTP/gRPC 服务器注册
│   ├── service/             # 服务层（实现 proto 接口）
│   ├── biz/                 # 业务逻辑层
│   └── data/                # 数据访问层
├── third_party/             # 第三方 proto 依赖
├── Makefile                 # 构建 & 生成脚本
├── Dockerfile               # 容器化
├── go.mod / go.sum
```

## API 设计

### Greeter 服务

- **方法**: `SayHello`
- **HTTP**: `GET /helloworld/{name}`
- **gRPC**: `helloworld.v1.Greeter/SayHello`

**请求**:
```protobuf
message HelloRequest {
  string name = 1;
}
```

**响应**:
```protobuf
message HelloReply {
  string message = 1;
}
```

## 分层架构

1. **API 层** (`api/`): Proto 定义，自动生成 HTTP/gRPC 代码
2. **Service 层** (`internal/service/`): 实现 proto 接口，处理请求/响应转换
3. **Biz 层** (`internal/biz/`): 核心业务逻辑，定义 Repo 接口
4. **Data 层** (`internal/data/`): 数据访问实现（本示例为内存数据）
5. **Server 层** (`internal/server/`): HTTP/gRPC 服务器配置和路由注册
6. **Conf 层** (`internal/conf/`): 配置结构体定义

## 中间件

按顺序应用以下中间件：
1. `recovery.Recovery()` — 异常恢复
2. `tracing.Server()` — 链路追踪
3. `logging.Server(logger)` — 请求日志

## 配置

```yaml
server:
  http:
    addr: 0.0.0.0:8000
    timeout: 1s
  grpc:
    addr: 0.0.0.0:9000
    timeout: 1s
```

## 实现步骤

1. 初始化 Go 模块
2. 安装 kratos CLI 工具
3. 创建 Proto 定义文件
4. 创建配置结构和配置文件
5. 实现 Biz 层业务逻辑
6. 实现 Data 层
7. 实现 Service 层
8. 实现 Server 层（HTTP + gRPC）
9. 实现 Wire 依赖注入
10. 实现 main.go 入口
11. 编写 Makefile
12. 编写 Dockerfile
13. 编译运行验证
