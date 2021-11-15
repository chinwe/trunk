package com.learn.ioc;

import com.learn.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author chinwe
 * 2021/10/24
 */
public class TestUser {

    @Test
    public void testFoo() {
        // 加载配置文件
        final ApplicationContext context =
                new ClassPathXmlApplicationContext("bean1.xml");

        // 获取配置创建的对象
        final User user = context.getBean("user", User.class);
    }
}
