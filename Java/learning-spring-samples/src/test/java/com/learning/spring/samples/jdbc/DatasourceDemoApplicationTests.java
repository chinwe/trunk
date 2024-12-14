package com.learning.spring.samples.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariProxyConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author chinwe
 * 2024/12/14
 */
@SpringBootTest
class DatasourceDemoApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testDataSource() throws SQLException {
        assertTrue(applicationContext.containsBean("dataSource"));
        DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
        assertInstanceOf(HikariDataSource.class, dataSource);

        Connection connection = dataSource.getConnection();
        assertInstanceOf(HikariProxyConnection.class, connection);
        connection.close();

        assertEquals(10, ((HikariDataSource) dataSource).getMaximumPoolSize());

        JdbcClient jdbcClient = JdbcClient.create(dataSource);
        assertEquals(1, jdbcClient.sql("select 1")
                .query(Integer.class)
                .single()
        );
    }
}