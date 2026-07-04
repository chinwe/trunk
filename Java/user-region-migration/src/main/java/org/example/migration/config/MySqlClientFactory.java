package org.example.migration.config;

import org.example.migration.client.ClientFactory;
import org.example.migration.client.JdbcMySqlClient;
import org.example.migration.client.RegionClient;
import org.example.migration.domain.ClientType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * MySQL 客户端工厂。支持多实例（同一 region 可配多个 MySQL 数据源）。
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
        DataSource dataSource = new DriverManagerDataSource(
                dsCfg.getJdbcUrl(), dsCfg.getUsername(), dsCfg.getPassword());
        return new JdbcMySqlClient(dataSource);
    }

    @Override
    public boolean supportsMultiInstance() {
        return true;
    }
}
