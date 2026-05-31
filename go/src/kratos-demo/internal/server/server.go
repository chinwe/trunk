package server

import "github.com/google/wire"

// ProviderSet 服务器层依赖注入集合
var ProviderSet = wire.NewSet(NewHTTPServer, NewGRPCServer)
