package com.learn.aop;

import com.learn.config.AppConfig;
import com.learn.entity.User;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * AopTest
 * @author chinwe
 * 2021/12/26
 */
public class AopTest {
    @Test
    public void test() {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        final IUserDao userDao = applicationContext.getBean(IUserDao.class);
        userDao.add(new User());
    }
}
