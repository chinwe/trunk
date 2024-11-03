package com.learning.spring.samples.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author chinwe
 * 2024/11/3
 */
@SpringBootTest
public class ShopReadyHealthIndicatorTest {

    @Autowired
    private HealthContributorRegistry registry;

    @Test
    void testRegistryContainsShopReady() {
        assertNotNull(registry.getContributor("shopReady"));
    }
}