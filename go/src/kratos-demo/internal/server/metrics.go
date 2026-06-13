package server

import (
	"net/http"

	"github.com/go-kratos/kratos/v2/middleware"
	"github.com/go-kratos/kratos/v2/middleware/metrics"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/collectors"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	otelprom "go.opentelemetry.io/otel/exporters/prometheus"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
)

// Metrics 封装 server 级可观测性指标：请求计数/延迟中间件与 /metrics 暴露 handler。
// HTTP 与 gRPC server 共享同一实例，counter/histogram 仅创建一次，以 kind 标签区分 transport。
type Metrics struct {
	// Middleware 请求指标中间件（请求计数 counter + 处理延迟 histogram），挂到 server 中间件链
	Middleware middleware.Middleware
	// HTTPHandler Prometheus 文本格式暴露 handler，挂到 HTTP server 的 /metrics 路由
	HTTPHandler http.Handler
}

// NewMetrics 创建 server 级指标实例。
// 使用独立 Prometheus registry（非全局默认），保证多实例/测试间互不干扰；
// 同时注册 Go runtime 与 process 指标，并把 OTel 请求指标桥接到 Prometheus。
func NewMetrics() (*Metrics, error) {
	// 独立 registry，避免全局状态污染
	reg := prometheus.NewRegistry()
	reg.MustRegister(collectors.NewGoCollector())
	reg.MustRegister(collectors.NewProcessCollector(collectors.ProcessCollectorOpts{}))

	// OTel Prometheus exporter 桥接到独立 registry
	exporter, err := otelprom.New(otelprom.WithRegisterer(reg))
	if err != nil {
		return nil, err
	}

	// MeterProvider：exporter 作为 Reader，histogram 显式 bucket view 控制直方图边界
	view := metrics.DefaultSecondsHistogramView(metrics.DefaultServerSecondsHistogramName)
	provider := sdkmetric.NewMeterProvider(
		sdkmetric.WithReader(exporter),
		sdkmetric.WithView(view),
	)
	meter := provider.Meter("kratos-demo")

	// 请求计数 counter 与处理延迟 histogram（沿用 kratos metrics 中间件的默认指标名）
	counter, err := metrics.DefaultRequestsCounter(meter, metrics.DefaultServerRequestsCounterName)
	if err != nil {
		return nil, err
	}
	histogram, err := metrics.DefaultSecondsHistogram(meter, metrics.DefaultServerSecondsHistogramName)
	if err != nil {
		return nil, err
	}

	return &Metrics{
		Middleware: metrics.Server(
			metrics.WithRequests(counter),
			metrics.WithSeconds(histogram),
		),
		HTTPHandler: promhttp.HandlerFor(reg, promhttp.HandlerOpts{}),
	}, nil
}
