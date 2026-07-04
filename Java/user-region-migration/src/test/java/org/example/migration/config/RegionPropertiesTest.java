package org.example.migration.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 配置层行为测试：验证 region 连接信息能从 application-test.yml 正确绑定到 RegionProperties。
 *
 * 用 MigrationAutoConfiguration 作为最小上下文（仅启用配置属性绑定，不含 Shell runner），
 * 既验证真实 yaml → 属性绑定，又避免 Spring Shell 交互式 runner 阻塞测试。
 */
@SpringBootTest(classes = MigrationAutoConfiguration.class, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class RegionPropertiesTest {

    @Autowired
    private RegionProperties regionProperties;

    @Test
    @DisplayName("能取到新加坡 region 的 MySQL 业务库连接配置")
    void shouldBindSingaporeMysqlBusinessConfig() {
        var mysql = regionProperties.getRegions().get("singapore").getMysql().get("business");

        assertThat(mysql).isNotNull();
        assertThat(mysql.getJdbcUrl()).isEqualTo("jdbc:mysql://test/sg_biz");
        assertThat(mysql.getUsername()).isEqualTo("sg_user");
        assertThat(mysql.getPassword()).isEqualTo("sg_pwd");
    }

    @Test
    @DisplayName("能取到缅甸 region 的 S3 配置")
    void shouldBindMyanmarS3Config() {
        var s3 = regionProperties.getRegions().get("myanmar").getS3();

        assertThat(s3).isNotNull();
        assertThat(s3.getBucket()).isEqualTo("mm-bucket");
        assertThat(s3.getEndpoint()).isEqualTo("http://test");
    }

    @Test
    @DisplayName("能取到新加坡 region 的 Kafka topic 前缀")
    void shouldBindSingaporeKafkaTopicPrefix() {
        var kafka = regionProperties.getRegions().get("singapore").getKafka();

        assertThat(kafka).isNotNull();
        assertThat(kafka.getTopicPrefix()).isEqualTo("singapore");
    }

    @Test
    @DisplayName("多个 region 同时绑定时互不干扰")
    void shouldBindMultipleRegionsIndependently() {
        var regions = regionProperties.getRegions();

        assertThat(regions).containsKeys("singapore", "myanmar");
        assertThat(regions.get("singapore").getKafka().getTopicPrefix()).isEqualTo("singapore");
        assertThat(regions.get("myanmar").getKafka().getTopicPrefix()).isEqualTo("myanmar");
    }
}
