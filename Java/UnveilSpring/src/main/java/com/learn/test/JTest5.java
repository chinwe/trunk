package com.learn.test;

import com.learn.aop.IUserDao;
import com.learn.config.AppConfig;
import com.learn.config.ApplicationContextHolder;
import com.learn.service.WindowsOnlyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author chinwe
 * 2022/1/16
 */
@SpringJUnitConfig(classes = AppConfig.class)
public class JTest5 {

    @Autowired
    private IUserDao userDao;

    @Test
    public void test() {
        assertNotNull(userDao);
    }

    @Test
    public void testConditional() {
        WindowsOnlyService windowsOnlyService = ApplicationContextHolder.getBean(WindowsOnlyService.class);
        assertTrue(windowsOnlyService.isWindows());
    }
}
