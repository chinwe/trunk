package com.learn.aop;

import com.learn.aop.impl.UserDaoImpl;
import com.learn.config.AppConfig;
import com.learn.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
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

    @Test
    public void createProxy() {
        UserDaoImpl userDaoImpl = new UserDaoImpl();

        // create a factory that can generate a proxy for the given target object
        AspectJProxyFactory factory = new AspectJProxyFactory(userDaoImpl);

        // add an aspect, the class must be an @AspectJ aspect
        // you can call this as many times as you need with different aspects
        factory.addAspect(UserDaoEnhance.class);

        final IUserDao userDao = factory.getProxy();
        userDao.add(new User());
    }
}
