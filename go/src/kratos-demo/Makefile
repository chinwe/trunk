.PHONY: all init api wire build run clean

# 项目名
APP_NAME = kratos-demo

# 生成所有代码
all: api wire

# 初始化依赖
init:
	go mod download
	go mod tidy

# 生成 proto 代码（使用 python grpc_tools 替代 protoc）
api:
	python -m grpc_tools.protoc --proto_path=. --proto_path=./third_party --go_out=paths=source_relative:. --go-http_out=paths=source_relative:. --go-grpc_out=paths=source_relative:. api/helloworld/v1/helloworld.proto
	python -m grpc_tools.protoc --proto_path=. --proto_path=./third_party --go_out=paths=source_relative:. internal/conf/conf.proto

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
