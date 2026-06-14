package data

import (
	"context"
	"errors"
	"testing"

	entsql "entgo.io/ent/dialect/sql"
	"github.com/DATA-DOG/go-sqlmock"
	"github.com/go-kratos/kratos/v2/log"

	"kratos-demo/internal/data/ent"
)

// newMockRepo 构造一个底层挂 sqlmock 的问候仓库,单测不依赖真实 MySQL。
// sqlmock 拦截 ent 下发的 SQL,校验问候记录确实被写入并回传期望结果。
func newMockRepo(t *testing.T) (*greeterRepo, sqlmock.Sqlmock) {
	t.Helper()
	db, mock, err := sqlmock.New()
	if err != nil {
		t.Fatalf("sqlmock.New() error: %v", err)
	}
	t.Cleanup(func() {
		if err := mock.ExpectationsWereMet(); err != nil {
			t.Errorf("unmet sqlmock expectations: %v", err)
		}
		_ = db.Close()
	})
	// ent driver 挂在 sqlmock 上,SQL 不会真正落库
	drv := entsql.OpenDB("mysql", db)
	client := ent.NewClient(ent.Driver(drv))
	return &greeterRepo{
		data: &Data{client: client},
		log:  log.NewHelper(log.DefaultLogger),
	}, mock
}

func TestGreeterRepo_CreateHello(t *testing.T) {
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
			repo, mock := newMockRepo(t)
			// 问候记录以 (name, message, created_at) 三列写入
			mock.ExpectExec("INSERT INTO `greetings`").
				WithArgs(tt.arg, tt.want, sqlmock.AnyArg()).
				WillReturnResult(sqlmock.NewResult(1, 1))

			got, err := repo.CreateHello(context.Background(), tt.arg)
			if err != nil {
				t.Fatalf("CreateHello() unexpected error: %v", err)
			}
			if got != tt.want {
				t.Errorf("CreateHello() = %q, want %q", got, tt.want)
			}
		})
	}
}

func TestGreeterRepo_CreateHello_DBError(t *testing.T) {
	repo, mock := newMockRepo(t)
	wantErr := errors.New("insert failed")
	mock.ExpectExec("INSERT INTO `greetings`").
		WillReturnError(wantErr)

	_, err := repo.CreateHello(context.Background(), "kratos")
	if !errors.Is(err, wantErr) {
		t.Errorf("CreateHello() err = %v, want %v", err, wantErr)
	}
}
