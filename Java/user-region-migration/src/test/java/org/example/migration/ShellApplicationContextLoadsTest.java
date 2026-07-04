package org.example.migration;

import org.example.migration.config.MigrationAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * 冒烟测试：验证框架核心配置上下文能正常加载。
 *
 * 不加载 ShellApplication 全量上下文——Spring Shell 的交互式 runner 在测试中
 * 会阻塞等待 stdin。这里只验证配置自动装配（RegionProperties/MigrationProperties）
 * 能成功启动，覆盖脚手架的核心可用性。
 */
@SpringBootTest(classes = MigrationAutoConfiguration.class, webEnvironment = WebEnvironment.NONE)
class ShellApplicationContextLoadsTest {

    @Test
    @DisplayName("框架配置上下文能成功启动")
    void contextLoads() {
        // 若上下文加载失败，此测试会直接报错；
        // 走到这里即证明配置自动装配与配置文件解析无误。
    }
}
