package service

import "github.com/google/wire"

// ProviderSet 服务层依赖注入集合
var ProviderSet = wire.NewSet(NewGreeterService)
