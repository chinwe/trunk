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

# proto 破坏性变更检测（monorepo 适配：git root 在上层 trunk，
# 标准 .git#branch=master 失效，取 master 的本目录子树再对比）
breaking:
	d=$$(mktemp -d); git archive master . | tar -x -C $$d && buf breaking --against $$d; status=$$?; rm -rf $$d; exit $$status

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
