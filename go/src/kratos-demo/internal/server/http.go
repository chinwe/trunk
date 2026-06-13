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
func NewHTTPServer(c *conf.Server, greeter *service.GreeterService, m *Metrics, logger log.Logger) *http.Server {
	log := log.NewHelper(logger)

	opts := []http.ServerOption{
		http.Middleware(
			recovery.Recovery(),
			tracing.Server(),
			logging.Server(logger),
			m.Middleware,
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
	// /metrics 直挂底层 mux，不经过 server 级中间件链，抓取不产生访问日志与 trace span
	srv.Handle("/metrics", m.HTTPHandler)

	log.Info("HTTP server configured")
	return srv
}
