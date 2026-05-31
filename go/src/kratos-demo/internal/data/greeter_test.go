package data

import (
	"context"
	"testing"

	"github.com/go-kratos/kratos/v2/log"
)

func TestGreeterRepo_CreateHello(t *testing.T) {
	repo := NewGreeterRepo(log.DefaultLogger)

	got, err := repo.CreateHello(context.Background(), "world")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	want := "Hello world"
	if got != want {
		t.Errorf("CreateHello() = %q, want %q", got, want)
	}
}

func TestGreeterRepo_CreateHello_EmptyName(t *testing.T) {
	repo := NewGreeterRepo(log.DefaultLogger)

	got, err := repo.CreateHello(context.Background(), "")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	want := "Hello "
	if got != want {
		t.Errorf("CreateHello() = %q, want %q", got, want)
	}
}

func TestGreeterRepo_CreateHello_SpecialChars(t *testing.T) {
	repo := NewGreeterRepo(log.DefaultLogger)

	name := "张三"
	got, err := repo.CreateHello(context.Background(), name)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	want := "Hello 张三"
	if got != want {
		t.Errorf("CreateHello() = %q, want %q", got, want)
	}
}
