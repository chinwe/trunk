package data

import (
	"context"
	"fmt"

	"kratos-demo/internal/biz"

	"github.com/go-kratos/kratos/v2/log"
)

// greeterRepo 问候仓库实现,基于 ent 客户端持久化问候记录。
type greeterRepo struct {
	data *Data
	log  *log.Helper
}

// NewGreeterRepo 创建问候仓库
func NewGreeterRepo(data *Data, logger log.Logger) biz.GreeterRepo {
	return &greeterRepo{
		data: data,
		log:  log.NewHelper(log.With(logger, "module", "data/greeter")),
	}
}

// CreateHello 生成问候消息并落库,返回问候消息。created_at 由 schema 默认值自动填充。
func (r *greeterRepo) CreateHello(ctx context.Context, name string) (string, error) {
	message := fmt.Sprintf("Hello %s", name)
	_, err := r.data.client.Greeting.Create().
		SetName(name).
		SetMessage(message).
		Save(ctx)
	if err != nil {
		return "", err
	}
	return message, nil
}
