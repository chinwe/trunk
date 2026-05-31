# kratos-demo 微服务实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建一个基于 go-kratos v2 的 Hello World 微服务，支持 HTTP + gRPC 双协议，采用标准分层架构。

**Architecture:** 使用 Protocol Buffers 定义 API，通过 protoc 生成 HTTP/gRPC 代码。采用 DDD 分层（service → biz → data），使用 Google Wire 进行编译时依赖注入。中间件链处理 recovery、tracing、logging。

**Tech Stack:** Go 1.25, go-kratos v2, Protocol Buffers 3, Google Wire, protoc-gen-go

**Spec:** `docs/superpowers/specs/2026-05-31-kratos-demo-design.md`

---

## File Structure

| File | Responsibility |
|------|---------------|
| `go.mod` | Go 模块定义 |
| `api/helloworld/v1/helloworld.proto` | Greeter 服务 Proto 定义 |
| `api/helloworld/v1/helloworld.pb.go` | protoc 生成 - 消息结构体 |
| `api/helloworld/v1/helloworld_grpc.pb.go` | protoc 生成 - gRPC 接口 |
| `api/helloworld/v1/helloworld_http.pb.go` | protoc 生成 - HTTP 路由 |
| `internal/conf/conf.proto` | 配置结构体 Proto 定义 |
| `internal/conf/conf.pb.go` | protoc 生成 - 配置结构体 |
| `configs/config.yaml` | YAML 配置文件 |
| `internal/biz/greeter.go` | 业务逻辑层 - Greeter 用例 |
| `internal/data/greeter.go` | 数据层 - Greeter 仓库实现 |
| `internal/service/greeter.go` | 服务层 - Greeter proto 接口实现 |
| `internal/server/http.go` | HTTP 服务器配置 |
| `internal/server/grpc.go` | gRPC 服务器配置 |
| `internal/server/server.go` | Server ProviderSet |
| `cmd/demo/wire.go` | Wire 依赖注入声明 |
| `cmd/demo/wire_gen.go` | Wire 自动生成 |
| `cmd/demo/main.go` | 程序入口 |
| `Makefile` | 构建和生成脚本 |
| `Dockerfile` | 容器化构建 |
| `internal/biz/biz.go` | Biz 层 ProviderSet |

---

## Task 1: 初始化项目并安装工具

**Files:**
- Create: `go.mod`

- [ ] **Step 1: 初始化 Go 模块**

```bash
cd D:/workspace/GitHub/trunk/go/src/kratos-demo
go mod init kratos-demo
```

- [ ] **Step 2: 安装 protoc 工具链**

```bash
# 安装 protoc-gen-go 和 protoc-gen-go-grpc
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

注意: Windows 上还需要安装 protoc 编译器本体。如果没有 protoc，可通过 `choco install protoc` 或从 https://github.com/protocolbuffers/protobuf/releases 下载。

- [ ] **Step 4: 安装 kratos protoc HTTP 插件**

```bash
go install github.com/go-kratos/kratos/cmd/protoc-gen-go-http/v2@latest
```

- [ ] **Step 5: 安装 Wire**

```bash
go install github.com/google/wire/cmd/wire@latest
```

- [ ] **Step 6: 添加项目核心依赖**

```bash
go get github.com/go-kratos/kratos/v2
go get github.com/go-kratos/kratos/v2/transport/http
go get github.com/go-kratos/kratos/v2/transport/grpc
go get github.com/go-kratos/kratos/v2/config
go get github.com/go-kratos/kratos/v2/config/file
go get github.com/go-kratos/kratos/v2/log
go get github.com/go-kratos/kratos/v2/middleware/recovery
go get github.com/go-kratos/kratos/v2/middleware/tracing
go get github.com/go-kratos/kratos/v2/middleware/logging
go get google.golang.org/grpc
go get google.golang.org/protobuf
go get github.com/google/wire
```

- [ ] **Step 7: 提交**

```bash
git add go.mod go.sum
git commit -m "chore: initialize Go module with kratos dependencies"
```

---

## Task 2: 创建 Proto API 定义

**Files:**
- Create: `api/helloworld/v1/helloworld.proto`
- [ ] **Step 1: 创建目录结构**

```bash
mkdir -p api/helloworld/v1
```

- [ ] **Step 2: 创建 helloworld.proto**

```protobuf
// api/helloworld/v1/helloworld.proto

syntax = "proto3";

package helloworld.v1;

option go_package = "kratos-demo/api/helloworld/v1;v1";

import "google/api/annotations.proto";

// Greeter 服务
service GreeterService {
  // 发送问候
  rpc SayHello (HelloRequest) returns (HelloReply) {
    option (google.api.http) = {
      get: "/helloworld/{name}"
    };
  }
}

message HelloRequest {
  string name = 1;
}

message HelloReply {
  string message = 1;
}
```

- [ ] **Step 3: 添加 google/api/annotations.proto 依赖**

```bash
mkdir -p third_party/google/api
```

创建 `third_party/google/api/annotations.proto`:

```protobuf
// third_party/google/api/annotations.proto
syntax = "proto3";

package google.api;

import "google/api/http.proto";
import "google/protobuf/descriptor.proto";

option go_package = "google.golang.org/genproto/googleapis/api/annotations;annotations";
option java_package = "com.google.api";
option java_outer_classname = "AnnotationsProto";
option java_multiple_files = true;
option objc_class_prefix = "GAPI";

extend google.protobuf.MethodOptions {
  HttpRule http = 72295728;
}
```

创建 `third_party/google/api/http.proto`:

```protobuf
// third_party/google/api/http.proto
syntax = "proto3";

package google.api;

option go_package = "google.golang.org/genproto/googleapis/api/annotations;annotations";
option java_package = "com.google.api";
option java_outer_classname = "HttpProto";
option java_multiple_files = true;
option objc_class_prefix = "GAPI";

message Http {
  repeated HttpRule rules = 1;
  bool fully_decode_reserved_expansion = 2;
}

message HttpRule {
  string selector = 1;
  oneof pattern {
    string get = 2;
    string put = 3;
    string post = 4;
    string delete = 5;
    string patch = 6;
    CustomHttpPattern custom = 8;
  }
  string body = 7;
  string response_body = 12;
  repeated HttpRule additional_bindings = 11;
}

message CustomHttpPattern {
  string kind = 1;
  string path = 2;
}
```

- [ ] **Step 4: 提交**

```bash
git add api/ third_party/
git commit -m "feat: add helloworld proto definition and third_party deps"
```

---

## Task 3: 创建配置 Proto 和配置文件

**Files:**
- Create: `internal/conf/conf.proto`
- Create: `internal/conf/conf.pb.go` (生成)
- Create: `configs/config.yaml`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p internal/conf
mkdir -p configs
```

- [ ] **Step 2: 创建 conf.proto**

```protobuf
// internal/conf/conf.proto

syntax = "proto3";

package kratos.demo.internal.conf;

option go_package = "kratos-demo/internal/conf;conf";

import "google/protobuf/duration.proto";

message Bootstrap {
  Server server = 1;
}

message Server {
  message HTTP {
    string addr = 1;
    google.protobuf.Duration timeout = 2;
  }
  message GRPC {
    string addr = 1;
    google.protobuf.Duration timeout = 2;
  }
  HTTP http = 1;
  GRPC grpc = 2;
}
```

- [ ] **Step 3: 创建 configs/config.yaml**

```yaml
server:
  http:
    addr: 0.0.0.0:8000
    timeout: 1s
  grpc:
    addr: 0.0.0.0:9000
    timeout: 1s
```

- [ ] **Step 4: 提交**

```bash
git add internal/conf/ configs/
git commit -m "feat: add configuration proto and YAML config"
```

---

## Task 4: 生成 Proto 代码

**Files:**
- Generate: `api/helloworld/v1/*.pb.go`
- Generate: `internal/conf/conf.pb.go`

- [ ] **Step 1: 生成 API proto 代码**

```bash
protoc --proto_path=. \
  --proto_path=./third_party \
  --go_out=paths=source_relative:. \
  --go-http_out=paths=source_relative:. \
  --go-grpc_out=paths=source_relative:. \
  api/helloworld/v1/helloworld.proto
```

- [ ] **Step 2: 生成配置 proto 代码**

```bash
protoc --proto_path=. \
  --proto_path=./third_party \
  --go_out=paths=source_relative:. \
  internal/conf/conf.proto
```

- [ ] **Step 3: 验证生成文件存在**

```bash
ls api/helloworld/v1/
ls internal/conf/
```

预期输出应包含 `.pb.go`, `_grpc.pb.go`, `_http.pb.go` 文件。

- [ ] **Step 4: 修复依赖并提交**

```bash
go mod tidy
git add api/ internal/conf/ go.mod go.sum
git commit -m "feat: generate protobuf code for API and config"
```

---

## Task 5: 实现 Biz 层（业务逻辑）

**Files:**
- Create: `internal/biz/greeter.go`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p internal/biz
```

- [ ] **Step 2: 创建 biz/greeter.go**

```go
// internal/biz/greeter.go

package biz

import (
	"context"

	"github.com/go-kratos/kratos/v2/log"
)

// GreeterRepo 定义数据访问接口
type GreeterRepo interface {
	// 创建问候记录
	CreateHello(ctx context.Context, name string) (string, error)
}

// GreeterUsecase 问候用例
type GreeterUsecase struct {
	repo GreeterRepo
	log  *log.Helper
}

// NewGreeterUsecase 创建问候用例
func NewGreeterUsecase(repo GreeterRepo, logger log.Logger) *GreeterUsecase {
	return &GreeterUsecase{
		repo: repo,
		log:  log.NewHelper(logger),
	}
}

// SayHello 执行问候逻辑
func (uc *GreeterUsecase) SayHello(ctx context.Context, name string) (string, error) {
	uc.log.WithContext(ctx).Infof("SayHello called with name: %s", name)
	return uc.repo.CreateHello(ctx, name)
}
```

- [ ] **Step 3: 提交**

```bash
git add internal/biz/
git commit -m "feat: implement biz layer with GreeterUsecase"
```

---

## Task 6: 实现 Data 层（数据访问）

**Files:**
- Create: `internal/data/greeter.go`
- Create: `internal/data/data.go`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p internal/data
```

- [ ] **Step 2: 创建 data/data.go**

```go
// internal/data/data.go

package data

import (
	"github.com/go-kratos/kratos/v2/log"
	"github.com/google/wire"
)

// ProviderSet 数据层依赖注入集合
var ProviderSet = wire.NewSet(NewData, NewGreeterRepo)

// Data 数据层结构（本示例不需要持久化，预留扩展）
type Data struct {
	log *log.Helper
}

// NewData 创建数据层实例
func NewData(logger log.Logger) (*Data, func(), error) {
	log := log.NewHelper(logger)
	log.Info("initializing data layer")

	cleanup := func() {
		log.Info("closing the data resources")
	}
	return &Data{log: log}, cleanup, nil
}
```

- [ ] **Step 3: 创建 data/greeter.go**

```go
// internal/data/greeter.go

package data

import (
	"context"
	"fmt"

	"github.com/go-kratos/kratos/v2/log"

	"kratos-demo/internal/biz"
)

// greeterRepo 问候仓库实现
type greeterRepo struct {
	data *Data
	log  *log.Helper
}

// NewGreeterRepo 创建问候仓库
func NewGreeterRepo(data *Data, logger log.Logger) biz.GreeterRepo {
	return &greeterRepo{
		data: data,
		log:  log.NewHelper(logger),
	}
}

// CreateHello 创建问候消息
func (r *greeterRepo) CreateHello(ctx context.Context, name string) (string, error) {
	r.log.WithContext(ctx).Infof("CreateHello: name=%s", name)
	return fmt.Sprintf("Hello %s", name), nil
}
```

- [ ] **Step 4: 提交**

```bash
git add internal/data/
git commit -m "feat: implement data layer with GreeterRepo"
```

---

## Task 7: 实现 Service 层（服务接口）

**Files:**
- Create: `internal/service/greeter.go`
- Create: `internal/service/service.go`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p internal/service
```

- [ ] **Step 2: 创建 service/service.go**

```go
// internal/service/service.go

package service

import "github.com/google/wire"

// ProviderSet 服务层依赖注入集合
var ProviderSet = wire.NewSet(NewGreeterService)
```

- [ ] **Step 3: 创建 service/greeter.go**

```go
// internal/service/greeter.go

package service

import (
	"context"

	pb "kratos-demo/api/helloworld/v1"
	"kratos-demo/internal/biz"

	"github.com/go-kratos/kratos/v2/log"
)

// GreeterService 问候服务实现
type GreeterService struct {
	pb.UnimplementedGreeterServiceServer

	uc  *biz.GreeterUsecase
	log *log.Helper
}

// NewGreeterService 创建问候服务
func NewGreeterService(uc *biz.GreeterUsecase, logger log.Logger) *GreeterService {
	return &GreeterService{
		uc:  uc,
		log: log.NewHelper(logger),
	}
}

// SayHello 处理问候请求
func (s *GreeterService) SayHello(ctx context.Context, req *pb.HelloRequest) (*pb.HelloReply, error) {
	s.log.WithContext(ctx).Infof("SayHello request: name=%s", req.Name)
	message, err := s.uc.SayHello(ctx, req.Name)
	if err != nil {
		return nil, err
	}
	return &pb.HelloReply{Message: message}, nil
}
```

- [ ] **Step 4: 提交**

```bash
git add internal/service/
git commit -m "feat: implement service layer with GreeterService"
```

---

## Task 8: 实现 Server 层（HTTP + gRPC 服务器）

**Files:**
- Create: `internal/server/http.go`
- Create: `internal/server/grpc.go`
- Create: `internal/server/server.go`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p internal/server
```

- [ ] **Step 2: 创建 server/server.go**

```go
// internal/server/server.go

package server

import "github.com/google/wire"

// ProviderSet 服务器层依赖注入集合
var ProviderSet = wire.NewSet(NewHTTPServer, NewGRPCServer)
```

- [ ] **Step 3: 创建 server/http.go**

```go
// internal/server/http.go

package server

import (
	"time"

	v1 "kratos-demo/api/helloworld/v1"
	"kratos-demo/internal/conf"
	"kratos-demo/internal/service"

	"github.com/go-kratos/kratos/v2/log"
	"github.com/go-kratos/kratos/v2/middleware/logging"
	"github.com/go-kratos/kratos/v2/middleware/recovery"
	"github.com/go-kratos/kratos/v2/middleware/tracing"
	"github.com/go-kratos/kratos/v2/transport/http"
)

// NewHTTPServer 创建 HTTP 服务器
func NewHTTPServer(c *conf.Server, greeter *service.GreeterService, logger log.Logger) *http.Server {
	log := log.NewHelper(logger)

	opts := []http.ServerOption{
		http.Middleware(
			recovery.Recovery(),
			tracing.Server(),
			logging.Server(logger),
		),
	}
	if c.Http != nil {
		opts = append(opts, http.Address(c.Http.Addr))
		if c.Http.Timeout != nil {
			opts = append(opts, http.Timeout(c.Http.Timeout.AsDuration()))
		} else {
			opts = append(opts, http.Timeout(time.Second))
		}
	}

	srv := http.NewServer(opts...)
	v1.RegisterGreeterServiceHTTPServer(srv, greeter)

	log.Info("HTTP server configured")
	return srv
}
```

- [ ] **Step 4: 创建 server/grpc.go**

```go
// internal/server/grpc.go

package server

import (
	"time"

	v1 "kratos-demo/api/helloworld/v1"
	"kratos-demo/internal/conf"
	"kratos-demo/internal/service"

	"github.com/go-kratos/kratos/v2/log"
	"github.com/go-kratos/kratos/v2/middleware/logging"
	"github.com/go-kratos/kratos/v2/middleware/recovery"
	"github.com/go-kratos/kratos/v2/middleware/tracing"
	"github.com/go-kratos/kratos/v2/transport/grpc"
)

// NewGRPCServer 创建 gRPC 服务器
func NewGRPCServer(c *conf.Server, greeter *service.GreeterService, logger log.Logger) *grpc.Server {
	log := log.NewHelper(logger)

	opts := []grpc.ServerOption{
		grpc.Middleware(
			recovery.Recovery(),
			tracing.Server(),
			logging.Server(logger),
		),
	}
	if c.Grpc != nil {
		opts = append(opts, grpc.Address(c.Grpc.Addr))
		if c.Grpc.Timeout != nil {
			opts = append(opts, grpc.Timeout(c.Grpc.Timeout.AsDuration()))
		} else {
			opts = append(opts, grpc.Timeout(time.Second))
		}
	}

	srv := grpc.NewServer(opts...)
	v1.RegisterGreeterServiceServer(srv, greeter)

	log.Info("gRPC server configured")
	return srv
}
```

- [ ] **Step 5: 提交**

```bash
git add internal/server/
git commit -m "feat: implement HTTP and gRPC server layer"
```

---

## Task 9: 实现 Wire 依赖注入和程序入口

**Files:**
- Create: `cmd/demo/wire.go`
- Create: `cmd/demo/wire_gen.go` (wire 生成)
- Create: `cmd/demo/main.go`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p cmd/demo
```

- [ ] **Step 2: 创建 cmd/demo/wire.go**

```go
//go:build wireinject
// +build wireinject

package main

import (
	"kratos-demo/internal/biz"
	"kratos-demo/internal/conf"
	"kratos-demo/internal/data"
	"kratos-demo/internal/server"
	"kratos-demo/internal/service"

	"github.com/go-kratos/kratos/v2"
	"github.com/go-kratos/kratos/v2/log"
	"github.com/google/wire"
)

// initApp 初始化 kratos 应用（wire 注入）
func initApp(*conf.Server, log.Logger) (*kratos.App, func(), error) {
	panic(wire.Build(
		server.ProviderSet,
		data.ProviderSet,
		biz.ProviderSet,
		service.ProviderSet,
		newApp,
	))
}
```

- [ ] **Step 3: 创建 cmd/demo/main.go**

```go
// cmd/demo/main.go

package main

import (
	"flag"
	"fmt"
	"os"

	"kratos-demo/internal/conf"

	"github.com/go-kratos/kratos/v2"
	"github.com/go-kratos/kratos/v2/config"
	"github.com/go-kratos/kratos/v2/config/file"
	"github.com/go-kratos/kratos/v2/log"
	"github.com/go-kratos/kratos/v2/transport/grpc"
	"github.com/go-kratos/kratos/v2/transport/http"
)

var flagconf string

func init() {
	// 配置文件路径参数
	flag.StringVar(&flagconf, "conf", "../../configs/config.yaml", "config path, eg: -conf config.yaml")
}

func main() {
	flag.Parse()

	// 创建 logger
	logger := log.With(log.NewStdLogger(os.Stdout),
		"ts",           log.DefaultTimestamp,
		"caller",       log.DefaultCaller,
		"service.name", "kratos-demo",
	)

	// 加载配置
	c := config.New(
		config.WithSource(
			file.NewSource(flagconf),
		),
	)
	defer c.Close()

	if err := c.Load(); err != nil {
		panic(err)
	}

	var bc conf.Bootstrap
	if err := c.Scan(&bc); err != nil {
		panic(err)
	}

	// 通过 wire 初始化应用
	app, cleanup, err := initApp(bc.Server, logger)
	if err != nil {
		panic(err)
	}
	defer cleanup()

	// 启动应用
	fmt.Println("Starting kratos-demo server...")
	if err := app.Run(); err != nil {
		panic(err)
	}
}

// newApp 创建 kratos 应用
func newApp(logger log.Logger, hs *http.Server, gs *grpc.Server) *kratos.App {
	return kratos.New(
		kratos.Name("kratos-demo"),
		kratos.Version("v1.0.0"),
		kratos.Metadata(map[string]string{}),
		kratos.Logger(logger),
		kratos.Server(hs, gs),
	)
}
```

- [ ] **Step 4: 添加 Biz 层 ProviderSet**

创建 `internal/biz/biz.go`:

```go
// internal/biz/biz.go

package biz

import "github.com/google/wire"

// ProviderSet 业务逻辑层依赖注入集合
var ProviderSet = wire.NewSet(NewGreeterUsecase)
```

- [ ] **Step 5: 生成 wire 代码**

```bash
cd cmd/demo
wire
cd ../..
```

- [ ] **Step 6: 修复依赖并提交**

```bash
go mod tidy
git add cmd/ internal/biz/biz.go go.mod go.sum
git commit -m "feat: implement Wire DI and main entry point"
```

---

## Task 10: 编写 Makefile

**Files:**
- Create: `Makefile`

- [ ] **Step 1: 创建 Makefile**

```makefile
.PHONY: all init api build run clean

# 项目名
APP_NAME = kratos-demo

# 生成所有代码
all: api

# 初始化依赖
init:
	go mod download
	go mod tidy

# 生成 proto 代码
api:
	protoc --proto_path=. \
		--proto_path=./third_party \
		--go_out=paths=source_relative:. \
		--go-http_out=paths=source_relative:. \
		--go-grpc_out=paths=source_relative:. \
		api/helloworld/v1/helloworld.proto

	protoc --proto_path=. \
		--proto_path=./third_party \
		--go_out=paths=source_relative:. \
		internal/conf/conf.proto

# 生成 wire
wire:
	cd cmd/demo && wire

# 编译
build:
	go build -o ./bin/ ./cmd/demo/...

# 运行（使用 go run 保证跨平台兼容）
run:
	go run ./cmd/demo/... -conf ./configs

# 清理
clean:
	rm -rf ./bin/
```

- [ ] **Step 2: 提交**

```bash
git add Makefile
git commit -m "build: add Makefile for build and proto generation"
```

---

## Task 11: 编写 Dockerfile

**Files:**
- Create: `Dockerfile`

- [ ] **Step 1: 创建 Dockerfile**

```dockerfile
# 构建阶段
FROM golang:1.25-alpine AS builder

WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app/demo ./cmd/demo

# 运行阶段
FROM alpine:latest

RUN apk --no-cache add ca-certificates
WORKDIR /app

COPY --from=builder /app/demo .
COPY configs/ ./configs/

EXPOSE 8000 9000

CMD ["./demo", "-conf", "./configs"]
```

- [ ] **Step 2: 提交**

```bash
git add Dockerfile
git commit -m "build: add Dockerfile for containerization"
```

---

## Task 12: 编译运行并验证

- [ ] **Step 1: 生成所有 proto 代码**

```bash
make api
```

预期: 无报错，生成 `.pb.go` 文件

- [ ] **Step 2: 生成 wire 代码**

```bash
make wire
```

预期: 生成 `cmd/demo/wire_gen.go`

- [ ] **Step 3: 修复依赖**

```bash
go mod tidy
```

- [ ] **Step 4: 编译项目**

```bash
make build
```

预期: 生成 `./bin/demo` 可执行文件

- [ ] **Step 5: 启动服务**

```bash
./bin/demo -conf ./configs
```

预期输出:
```
Starting kratos-demo server...
INFO msg=HTTP server configured
INFO msg=gRPC server configured
```

- [ ] **Step 6: 测试 HTTP 接口**

在另一个终端:
```bash
curl http://localhost:8000/helloworld/world
```

预期响应:
```json
{"message":"Hello world"}
```

- [ ] **Step 7: 测试 gRPC 接口**

```bash
grpcurl -plaintext -d '{"name":"kratos"}' localhost:9000 helloworld.v1.GreeterService/SayHello
```

预期响应:
```json
{
  "message": "Hello kratos"
}
```

- [ ] **Step 8: 最终提交**

```bash
git add .
git commit -m "feat: complete kratos-demo microservice with HTTP+gRPC support"
```
