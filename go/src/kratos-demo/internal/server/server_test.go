package server

import (
	"context"
	"net/http"
	"testing"
	"time"

	"kratos-demo/internal/biz"
	"kratos-demo/internal/conf"
	"kratos-demo/internal/service"

	"github.com/go-kratos/kratos/v2/log"
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

func TestNewHTTPServer(t *testing.T) {
	t.Run("default config", func(t *testing.T) {
		svr := NewHTTPServer(&conf.Server{}, buildTestService(t), log.DefaultLogger)
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
		}, buildTestService(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewHTTPServer() returned nil")
		}
	})

	t.Run("with address without timeout", func(t *testing.T) {
		svr := NewHTTPServer(&conf.Server{
			Http: &conf.Server_HTTP{
				Addr: ":0",
			},
		}, buildTestService(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewHTTPServer() returned nil")
		}
	})
}

func TestNewGRPCServer(t *testing.T) {
	t.Run("default config", func(t *testing.T) {
		svr := NewGRPCServer(&conf.Server{}, buildTestService(t), log.DefaultLogger)
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
		}, buildTestService(t), log.DefaultLogger)
		if svr == nil {
			t.Fatal("NewGRPCServer() returned nil")
		}
	})

	t.Run("with address without timeout", func(t *testing.T) {
		svr := NewGRPCServer(&conf.Server{
			Grpc: &conf.Server_GRPC{
				Addr: ":0",
			},
		}, buildTestService(t), log.DefaultLogger)
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
	}, buildTestService(t), log.DefaultLogger)

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
