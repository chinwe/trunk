package org.example.migration.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.migration.client.ClientFactory;
import org.example.migration.client.JdbcMySqlClient;
import org.example.migration.client.RegionClient;
import org.example.migration.domain.ClientType;

import javax.sql.DataSource;

/**
 * MySQL 客户端工厂。支持多实例（同一 region 可配多个 MySQL 数据源）。
 *
 * <p>用 HikariCP 连接池（Q5）：批量迁移大量数据时 {@code DriverManagerDataSource}
 * 每次申请都新建连接，性能灾难；HikariCP 提供连接池化与超时控制。
 */
public class MySqlClientFactory implements ClientFactory {

    @Override
    public ClientType supportedType() {
        return ClientType.MYSQL;
    }

    @Override
    public RegionClient create(RegionProperties.RegionConfig config, String instanceName) {
        if (config.getMysql() == null || !config.getMysql().containsKey(instanceName)) {
            return null;
        }
        RegionProperties.DataSourceConfig dsCfg = config.getMysql().get(instanceName);
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(dsCfg.getJdbcUrl());
        hc.setUsername(dsCfg.getUsername());
        hc.setPassword(dsCfg.getPassword());
        // 池大小保守默认；批量迁移并发度由 migration.default-threads 控制
        hc.setMaximumPoolSize(10);
        hc.setPoolName("migration-mysql-" + instanceName);
        // 不在初始化时强制建立连接：测试/装配场景下目标 DB 可能尚未就绪。
        // 真实连接错误在第一次 query 时即刻暴露（fail-fast 于使用点）。
        hc.setInitializationFailTimeout(-1);
        DataSource dataSource = new HikariDataSource(hc);
        return new JdbcMySqlClient(dataSource);
    }

    @Override
    public boolean supportsMultiInstance() {
        return true;
    }
}
