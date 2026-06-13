package biz

import (
	"context"

	"github.com/go-kratos/kratos/v2/log"
)

// GreeterRepo 定义数据访问接口
type GreeterRepo interface {
	// 创建问候记录
	CreateHello(ctx context.Context, name string) (string, error)
}

// GreeterUsecase 问候用例
type GreeterUsecase struct {
	repo GreeterRepo
	log  *log.Helper
}

// NewGreeterUsecase 创建问候用例
func NewGreeterUsecase(repo GreeterRepo, logger log.Logger) *GreeterUsecase {
	return &GreeterUsecase{
		repo: repo,
		log:  log.NewHelper(logger),
	}
}

// SayHello 执行问候逻辑
func (uc *GreeterUsecase) SayHello(ctx context.Context, name string) (string, error) {
	return uc.repo.CreateHello(ctx, name)
}
