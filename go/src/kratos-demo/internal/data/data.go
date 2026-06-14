package data

import (
	"context"
	"os"

	"kratos-demo/internal/conf"
	"kratos-demo/internal/data/ent"

	"github.com/go-kratos/kratos/v2/log"
	"github.com/google/wire"
	// 注册 MySQL 驱动,使 ent.Open("mysql", dsn) 可用
	_ "github.com/go-sql-driver/mysql"
)

// ProviderSet 数据层依赖注入集合
var ProviderSet = wire.NewSet(NewData, NewGreeterRepo)

// Data 数据层核心结构,持有 ent 客户端,被所有 repo 共享。
type Data struct {
	client *ent.Client
	log    *log.Helper
}

// NewData 创建数据层实例。
// DSN 从配置读取并通过环境变量展开(配置文件只放 ${VAR} 占位,明文密码不入库)。
// 启动时执行 schema 自动迁移(幂等建表);DB 不可用时返回错误,服务不启动。
func NewData(c *conf.Data, logger log.Logger) (*Data, func(), error) {
	log := log.NewHelper(log.With(logger, "module", "data"))

	// DSN 从环境变量注入,配置文件不留明文密码
	dsn := os.ExpandEnv(c.Database.GetSource())
	driver := c.Database.GetDriver()

	client, err := ent.Open(driver, dsn)
	if err != nil {
		return nil, nil, err
	}

	// 启动时自动建表(demo 场景);生产环境建议用独立迁移工具
	if err := client.Schema.Create(context.Background()); err != nil {
		_ = client.Close()
		return nil, nil, err
	}

	log.Infof("data layer initialized with driver=%s", driver)

	d := &Data{
		client: client,
		log:    log,
	}
	cleanup := func() {
		log.Info("closing data layer")
		if err := client.Close(); err != nil {
			log.Error(err)
		}
	}
	return d, cleanup, nil
}
