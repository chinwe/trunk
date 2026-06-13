package data

import (
	"context"
	"testing"

	"github.com/go-kratos/kratos/v2/log"
)

func TestGreeterRepo_CreateHello(t *testing.T) {
	repo := NewGreeterRepo(log.DefaultLogger)

	tests := []struct {
		name string
		arg  string
		want string
	}{
		{"normal", "world", "Hello world"},
		{"empty", "", "Hello "},
		{"unicode", "张三", "Hello 张三"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, err := repo.CreateHello(context.Background(), tt.arg)
			if err != nil {
				t.Fatalf("unexpected error: %v", err)
			}
			if got != tt.want {
				t.Errorf("CreateHello() = %q, want %q", got, tt.want)
			}
		})
	}
}
