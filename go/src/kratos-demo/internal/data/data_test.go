package data

import (
	"testing"

	"github.com/go-kratos/kratos/v2/log"

	"kratos-demo/internal/conf"
)

// TestNewGreeterRepo 验证问候仓库构造函数的依赖注入。
// 该函数本身无业务分支,其存在价值在于正确连接 wire 依赖链:
// 若 Data/logger 注入错误,下游 CreateHello 会触发 nil panic。
// 故测试编码的意图是"依赖正确装配",而非逐行行为。
func TestNewGreeterRepo(t *testing.T) {
	data := &Data{}
	repo := NewGreeterRepo(data, log.DefaultLogger)

	if repo == nil {
		t.Fatal("NewGreeterRepo() returned nil")
	}

	// 断言返回值是 *greeterRepo 且 data 字段被正确注入
	gr, ok := repo.(*greeterRepo)
	if !ok {
		t.Fatalf("NewGreeterRepo() returned %T, want *greeterRepo", repo)
	}
	if gr.data != data {
		t.Errorf("greeterRepo.data not injected: got %v, want %v", gr.data, data)
	}
	if gr.log == nil {
		t.Error("greeterRepo.log is nil, expected log.Helper to be initialized")
	}
}

// TestNewData_InvalidDSN 覆盖 NewData 的数据库连接错误分支(data.go:36-39)。
// dsn 为空时 ent.Open 在驱动层即返回错误,无需触及 schema 迁移路径。
func TestNewData_InvalidDSN(t *testing.T) {
	// DATABASE_SOURCE 留空,ent.Open("mysql", "") 必然连接失败
	t.Setenv("DATABASE_SOURCE", "")

	c := &conf.Data{
		Database: &conf.Data_Database{Driver: "mysql"},
	}

	data, cleanup, err := NewData(c, log.DefaultLogger)
	if err == nil {
		// 即便侥幸成功也必须清理资源,避免连接泄漏
		if cleanup != nil {
			cleanup()
		}
		t.Fatal("NewData() with empty DSN expected error, got nil")
	}
	if data != nil {
		t.Errorf("NewData() data = %v, want nil on error", data)
	}
	if cleanup != nil {
		t.Error("NewData() cleanup should be nil on error, got non-nil func")
	}
}

// 覆盖缺口说明(准则12:失败要响亮,不静默跳过):
// NewData 的 schema 迁移成功路径(data.go:42-59)当前未覆盖。
// 原因:ent 的 Schema.Create 内部会发多轮探测性 schema 查询(SHOW/SELECT),
// 用 sqlmock 精确匹配这些内部交互极其脆弱、绑定 ent 实现细节、升级即挂。
// 该路径应改用 enttest + 内存 SQLite 做集成测试覆盖,见 ADR/TODO 跟进。
