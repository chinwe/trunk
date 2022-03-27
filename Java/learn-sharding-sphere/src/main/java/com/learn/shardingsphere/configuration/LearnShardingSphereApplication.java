package com.learn.shardingsphere.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author chinwe
 * 2022/3/19
 */
@SpringBootApplication(scanBasePackages = {"com.learn.shardingsphere"})
@EnableJpaRepositories(basePackages = {"com.learn.shardingsphere.repository"})
@EntityScan(basePackages = {"com.learn.shardingsphere.repository.entity"})
public class LearnShardingSphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(LearnShardingSphereApplication.class, args);
    }
}
