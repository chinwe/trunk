package service

import (
	"context"
	"testing"

	pb "kratos-demo/api/helloworld/v1"
	"kratos-demo/internal/biz"

	"github.com/go-kratos/kratos/v2/log"
)

// mockRepo 是 biz.GreeterRepo 的测试替身
type mockRepo struct {
	response string
	err      error
}

func (m *mockRepo) CreateHello(_ context.Context, _ string) (string, error) {
	return m.response, m.err
}

func TestGreeterService_SayHello(t *testing.T) {
	// 构造依赖链：mockRepo → GreeterUsecase → GreeterService
	repo := &mockRepo{response: "Hello kratos"}
	uc := biz.NewGreeterUsecase(repo, log.DefaultLogger)
	svc := NewGreeterService(uc, log.DefaultLogger)

	req := &pb.HelloRequest{Name: "kratos"}
	resp, err := svc.SayHello(context.Background(), req)
	if err != nil {
		t.Fatalf("SayHello() unexpected error: %v", err)
	}
	if resp.Message != "Hello kratos" {
		t.Errorf("SayHello().Message = %q, want %q", resp.Message, "Hello kratos")
	}
}

func TestGreeterService_SayHello_EmptyName(t *testing.T) {
	repo := &mockRepo{response: "Hello "}
	uc := biz.NewGreeterUsecase(repo, log.DefaultLogger)
	svc := NewGreeterService(uc, log.DefaultLogger)

	req := &pb.HelloRequest{Name: ""}
	resp, err := svc.SayHello(context.Background(), req)
	if err != nil {
		t.Fatalf("SayHello() unexpected error: %v", err)
	}
	if resp.Message != "Hello " {
		t.Errorf("SayHello().Message = %q, want %q", resp.Message, "Hello ")
	}
}

func TestGreeterService_SayHello_RepoError(t *testing.T) {
	repo := &mockRepo{err: context.DeadlineExceeded}
	uc := biz.NewGreeterUsecase(repo, log.DefaultLogger)
	svc := NewGreeterService(uc, log.DefaultLogger)

	req := &pb.HelloRequest{Name: "kratos"}
	_, err := svc.SayHello(context.Background(), req)
	if err != context.DeadlineExceeded {
		t.Errorf("SayHello() err = %v, want %v", err, context.DeadlineExceeded)
	}
}
