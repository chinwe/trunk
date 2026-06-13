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
func NewGRPCServer(c *conf.Server, greeter *service.GreeterService, m *Metrics, logger log.Logger) *grpc.Server {
	log := log.NewHelper(logger)

	opts := []grpc.ServerOption{
		grpc.Middleware(
			recovery.Recovery(),
			tracing.Server(),
			logging.Server(logger),
			m.Middleware,
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
