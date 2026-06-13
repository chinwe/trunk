package data

import (
	"context"
	"fmt"

	"github.com/go-kratos/kratos/v2/log"
	"github.com/google/wire"

	"kratos-demo/internal/biz"
)

// ProviderSet 数据层依赖注入集合
var ProviderSet = wire.NewSet(NewGreeterRepo)

// greeterRepo 问候仓库实现
type greeterRepo struct {
	log *log.Helper
}

// NewGreeterRepo 创建问候仓库
func NewGreeterRepo(logger log.Logger) biz.GreeterRepo {
	return &greeterRepo{
		log: log.NewHelper(log.With(logger, "module", "data/greeter")),
	}
}

// CreateHello 创建问候消息
func (r *greeterRepo) CreateHello(ctx context.Context, name string) (string, error) {
	return fmt.Sprintf("Hello %s", name), nil
}
