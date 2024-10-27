package com.learning.spring.samples.config;

import com.learning.spring.samples.LearningSpringSamplesApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author chinwe
 * 2024/10/27
 */
@SpringBootTest(classes = LearningSpringSamplesApplication.class, properties = {
        "binarytea.ready=true",
        "binarytea.open-hours=8:30-22:00"
})
public class ShopConfigurationEnableTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void testPropertiesBeanAvailable() {
        assertNotNull(applicationContext.getBean(BinaryTeaProperties.class));
        assertTrue(applicationContext
                .containsBean("binarytea-com.learning.spring.samples.config.BinaryTeaProperties"));
    }

    @Test
    void testPropertyValues() {
        BinaryTeaProperties properties = applicationContext.getBean(BinaryTeaProperties.class);
        assertTrue(properties.isReady());
        assertEquals("8:30-22:00", properties.getOpenHours());
    }
}
