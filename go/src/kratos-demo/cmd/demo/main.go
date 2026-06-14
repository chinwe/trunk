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
	app, cleanup, err := initApp(bc.Server, bc.Data, logger)
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
