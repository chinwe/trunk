package com.learn.test;

import com.learn.aop.IUserDao;
import com.learn.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
