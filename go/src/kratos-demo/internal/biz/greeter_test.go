package biz

import (
	"context"
	"testing"

	"github.com/go-kratos/kratos/v2/log"
)

// mockRepo 是 GreeterRepo 的测试替身
type mockRepo struct {
	response string
	err      error
}

func (m *mockRepo) CreateHello(_ context.Context, _ string) (string, error) {
	return m.response, m.err
}

func TestGreeterUsecase_SayHello(t *testing.T) {
	uc := NewGreeterUsecase(
		&mockRepo{response: "Hello kratos"},
		log.DefaultLogger,
	)

	got, err := uc.SayHello(context.Background(), "kratos")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if got != "Hello kratos" {
		t.Errorf("SayHello() = %q, want %q", got, "Hello kratos")
	}
}
