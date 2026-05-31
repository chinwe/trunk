package data

import (
	"github.com/google/wire"
)

// ProviderSet 数据层依赖注入集合
var ProviderSet = wire.NewSet(NewGreeterRepo)
