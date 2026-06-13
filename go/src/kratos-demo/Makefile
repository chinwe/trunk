.PHONY: all init api wire build test run clean lint breaking

# 项目名
APP_NAME = kratos-demo

# 生成所有代码
all: api wire

# 初始化依赖
init:
	go mod download
	go mod tidy

# 生成 proto 代码（使用 buf CLI）
api:
	buf generate

# 更新 buf 远程依赖
buf-update:
	buf dep update

# proto lint 检查
lint:
	buf lint

# proto 破坏性变更检测
breaking:
	buf breaking --against '.git#branch=master'

# 生成 wire
wire:
	cd cmd/demo && wire

# 编译
build:
	go build -o ./bin/ ./cmd/demo/...

# 测试
test:
	go test ./...

# 运行（使用 go run 保证跨平台兼容）
run:
	go run ./cmd/demo/... -conf ./configs

# 清理生成文件
clean:
	rm -rf ./bin/
