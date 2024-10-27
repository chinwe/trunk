package com.learning.spring.samples.config;

import com.learning.spring.samples.LearningSpringSamplesApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author chinwe
 * 2024/10/27
 */
@SpringBootTest(classes = LearningSpringSamplesApplication.class, properties = {
        "binarytea.ready=false"
})
public class ShopConfigurationDisableTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testPropertiesBeanUnavailable() {
        assertEquals("false", applicationContext.getEnvironment().getProperty("binarytea.ready"));
        assertFalse(applicationContext.containsBean("binarytea-com.learning.spring.samples.config.BinaryTeaProperties"));
    }
}