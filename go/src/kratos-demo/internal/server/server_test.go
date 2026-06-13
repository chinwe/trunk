package server

import (
	"context"
	"io"
	"net/http"
	"strings"
	"testing"
	"time"

	v1 "kratos-demo/api/helloworld/v1"
	"kratos-demo/internal/biz"
	"kratos-demo/internal/conf"
	"kratos-demo/internal/service"

	"github.com/go-kratos/kratos/v2/log"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/protobuf/types/known/durationpb"
)

// mockRepo 是 biz.GreeterRepo 的测试替身
type mockRepo struct {
	response string
	err      error
}

func (m *mockRepo) CreateHello(_ context.Context, _ string) (string, error) {
	return m.response, m.err
}

// buildTestService 构建一个带 mock 依赖的 GreeterService 供测试使用
func buildTestService(t *testing.T) *service.GreeterService {
	t.Helper()
	repo := &mockRepo{response: "Hello kratos"}
	uc := biz.NewGreeterUsecase(repo, log.DefaultLogger)
	return service.NewGreeterService(uc, log.DefaultLogger)
}

// buildTestMetrics 构建一个独立的 Metrics 实例（独立 registry），保证测试间互不干扰
func buildTestMetrics(t *testing.T) *Metrics {
	t.Helper()
	m, err := NewMetrics()
	if err != nil {
		t.Fatalf("NewMetrics() error: %v", err)
	}
	return m
}

func TestNewHTTPServer(t *testing.T) {
	t.Run("default config", func(t *testing.T) {
		svr := NewHTTPServer(&conf.Server{}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewHTTPServer() returned nil")
		}
	})

	t.Run("with address", func(t *testing.T) {
		svr := NewHTTPServer(&conf.Server{
			Http: &conf.Server_HTTP{
				Addr:    ":0",
				Timeout: durationpb.New(time.Second),
			},
		}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewHTTPServer() returned nil")
		}
	})

	t.Run("with address without timeout", func(t *testing.T) {
		svr := NewHTTPServer(&conf.Server{
			Http: &conf.Server_HTTP{
				Addr: ":0",
			},
		}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewHTTPServer() returned nil")
		}
	})
}

func TestNewGRPCServer(t *testing.T) {
	t.Run("default config", func(t *testing.T) {
		svr := NewGRPCServer(&conf.Server{}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewGRPCServer() returned nil")
		}
	})

	t.Run("with address", func(t *testing.T) {
		svr := NewGRPCServer(&conf.Server{
			Grpc: &conf.Server_GRPC{
				Addr:    ":0",
				Timeout: durationpb.New(time.Second),
			},
		}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewGRPCServer() returned nil")
		}
	})

	t.Run("with address without timeout", func(t *testing.T) {
		svr := NewGRPCServer(&conf.Server{
			Grpc: &conf.Server_GRPC{
				Addr: ":0",
			},
		}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewGRPCServer() returned nil")
		}
	})
}

func TestHTTPServer_Integration(t *testing.T) {
	// 在随机端口启动 HTTP 服务器
	svr := NewHTTPServer(&conf.Server{
		Http: &conf.Server_HTTP{
			Addr:    ":0",
			Timeout: durationpb.New(time.Second * 5),
		},
	}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)

	// 启动服务器（goroutine，避免阻塞）
	go func() {
		if err := svr.Start(context.Background()); err != nil {
			t.Errorf("HTTP server start error: %v", err)
		}
	}()
	defer svr.Stop(context.Background())

	// 等待服务器就绪并获取地址
	time.Sleep(200 * time.Millisecond)
	endpoint, err := svr.Endpoint()
	if err != nil {
		t.Fatalf("Endpoint() error: %v", err)
	}

	// 发送 HTTP GET 请求
	url := endpoint.String() + "/helloworld/kratos"
	resp, err := http.Get(url)
	if err != nil {
		t.Fatalf("HTTP GET %s failed: %v", url, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Errorf("HTTP status = %d, want %d", resp.StatusCode, http.StatusOK)
	}
}

// TestHTTPServer_Metrics 验证 HTTP server 暴露 /metrics，并记录带 kind="http" 标签的 SayHello 请求计数，
// 同时确认 Go runtime 指标出现（tracer bullet：端到端打通 OTel→Prometheus→/metrics 链路）。
func TestHTTPServer_Metrics(t *testing.T) {
	svr := NewHTTPServer(&conf.Server{
		Http: &conf.Server_HTTP{
			Addr:    ":0",
			Timeout: durationpb.New(time.Second * 5),
		},
	}, buildTestService(t), buildTestMetrics(t), log.DefaultLogger)

	go func() {
		if err := svr.Start(context.Background()); err != nil {
			t.Errorf("HTTP server start error: %v", err)
		}
	}()
	defer svr.Stop(context.Background())

	time.Sleep(200 * time.Millisecond)
	endpoint, err := svr.Endpoint()
	if err != nil {
		t.Fatalf("Endpoint() error: %v", err)
	}

	// 触发一次 SayHello 请求，产生请求计数指标
	helloURL := endpoint.String() + "/helloworld/kratos"
	if resp, err := http.Get(helloURL); err != nil {
		t.Fatalf("HTTP GET %s failed: %v", helloURL, err)
	} else {
		resp.Body.Close()
	}

	// 抓取 /metrics
	metricsURL := endpoint.String() + "/metrics"
	mresp, err := http.Get(metricsURL)
	if err != nil {
		t.Fatalf("HTTP GET %s failed: %v", metricsURL, err)
	}
	defer mresp.Body.Close()

	if mresp.StatusCode != http.StatusOK {
		t.Fatalf("/metrics status = %d, want %d", mresp.StatusCode, http.StatusOK)
	}

	body, err := io.ReadAll(mresp.Body)
	if err != nil {
		t.Fatalf("read /metrics body failed: %v", err)
	}
	bodyStr := string(body)

	// SayHello 请求计数指标带 kind="http" 标签（fuzzy 匹配，不 pin metric family 名）
	if !strings.Contains(bodyStr, `kind="http"`) {
		t.Errorf("/metrics body missing SayHello request counter with kind=\"http\"\n--- body ---\n%s", bodyStr)
	}

	// Go runtime 指标出现
	if !strings.Contains(bodyStr, "go_goroutines") {
		t.Errorf("/metrics body missing Go runtime metrics\n--- body ---\n%s", bodyStr)
	}
}

// TestGRPCServer_Metrics 验证 gRPC 入口的 SayHello 请求被同一组指标记录（kind="grpc"），
// 并经共享的 HTTP /metrics 暴露——确认 HTTP 与 gRPC 共享同一组指标实例的核心承诺。
func TestGRPCServer_Metrics(t *testing.T) {
	// gRPC 与 HTTP server 共享同一个 Metrics 实例（同一组 counter/histogram/registry）
	m := buildTestMetrics(t)

	// gRPC server（带 metrics 中间件）
	gsvr := NewGRPCServer(&conf.Server{
		Grpc: &conf.Server_GRPC{
			Addr:    ":0",
			Timeout: durationpb.New(time.Second * 5),
		},
	}, buildTestService(t), m, log.DefaultLogger)
	go func() {
		if err := gsvr.Start(context.Background()); err != nil {
			t.Errorf("gRPC server start error: %v", err)
		}
	}()
	defer gsvr.Stop(context.Background())

	// HTTP server（共享 Metrics，暴露 /metrics）
	hsvr := NewHTTPServer(&conf.Server{
		Http: &conf.Server_HTTP{
			Addr:    ":0",
			Timeout: durationpb.New(time.Second * 5),
		},
	}, buildTestService(t), m, log.DefaultLogger)
	go func() {
		if err := hsvr.Start(context.Background()); err != nil {
			t.Errorf("HTTP server start error: %v", err)
		}
	}()
	defer hsvr.Stop(context.Background())

	time.Sleep(200 * time.Millisecond)

	// gRPC client 调 SayHello，产生 gRPC 请求指标
	gEndpoint, err := gsvr.Endpoint()
	if err != nil {
		t.Fatalf("gRPC Endpoint() error: %v", err)
	}
	conn, err := grpc.NewClient(gEndpoint.Host, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		t.Fatalf("grpc.NewClient error: %v", err)
	}
	defer conn.Close()

	client := v1.NewGreeterServiceClient(conn)
	reply, err := client.SayHello(context.Background(), &v1.HelloRequest{Name: "kratos"})
	if err != nil {
		t.Fatalf("SayHello gRPC call failed: %v", err)
	}
	if reply.GetMessage() != "Hello kratos" {
		t.Fatalf("SayHello message = %q, want %q", reply.GetMessage(), "Hello kratos")
	}

	// 抓取 HTTP /metrics
	hEndpoint, err := hsvr.Endpoint()
	if err != nil {
		t.Fatalf("HTTP Endpoint() error: %v", err)
	}
	mresp, err := http.Get(hEndpoint.String() + "/metrics")
	if err != nil {
		t.Fatalf("HTTP GET /metrics failed: %v", err)
	}
	defer mresp.Body.Close()

	if mresp.StatusCode != http.StatusOK {
		t.Fatalf("/metrics status = %d, want %d", mresp.StatusCode, http.StatusOK)
	}

	body, err := io.ReadAll(mresp.Body)
	if err != nil {
		t.Fatalf("read /metrics body failed: %v", err)
	}

	// 断言：gRPC SayHello 请求计数带 kind="grpc" 标签
	if !strings.Contains(string(body), `kind="grpc"`) {
		t.Errorf("/metrics body missing gRPC SayHello request counter with kind=\"grpc\"\n--- body ---\n%s", string(body))
	}
}
