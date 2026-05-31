package data

import (
	"github.com/go-kratos/kratos/v2/log"
	"github.com/google/wire"
)

// ProviderSet 数据层依赖注入集合
var ProviderSet = wire.NewSet(NewData, NewGreeterRepo)

// Data 数据层结构（本示例不需要持久化，预留扩展）
type Data struct {
	log *log.Helper
}

// NewData 创建数据层实例
func NewData(logger log.Logger) (*Data, func(), error) {
	log := log.NewHelper(logger)
	log.Info("initializing data layer")

	cleanup := func() {
		log.Info("closing the data resources")
	}
	return &Data{log: log}, cleanup, nil
}
