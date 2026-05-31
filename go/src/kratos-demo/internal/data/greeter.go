package data

import (
	"context"
	"fmt"

	"github.com/go-kratos/kratos/v2/log"

	"kratos-demo/internal/biz"
)

// greeterRepo 问候仓库实现
type greeterRepo struct {
	log *log.Helper
}

// NewGreeterRepo 创建问候仓库
func NewGreeterRepo(logger log.Logger) biz.GreeterRepo {
	return &greeterRepo{
		log: log.NewHelper(logger),
	}
}

// CreateHello 创建问候消息
func (r *greeterRepo) CreateHello(ctx context.Context, name string) (string, error) {
	r.log.WithContext(ctx).Infof("CreateHello: name=%s", name)
	return fmt.Sprintf("Hello %s", name), nil
}
