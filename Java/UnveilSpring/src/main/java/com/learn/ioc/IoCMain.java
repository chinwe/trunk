package com.learn.ioc;

import com.learn.config.AppConfig;
import com.learn.config.ApplicationContextHolder;
import com.learn.service.UserService;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;


/**
 * @author chinwe
 * 2021/10/21
 */
public class IoCMain {
    public static void main(String[] args) {
        // BeanFactory
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.registerBeanDefinition(String.class.getName(), BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());

        final String bean = beanFactory.getBean(String.class);
        System.out.println(bean);

        // ApplicationContext
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        final UserService userService = applicationContext.getBean(UserService.class);

        final UserService userService1 = ApplicationContextHolder.getBean(UserService.class);

        ResourceLoader resourceLoader = new DefaultResourceLoader();
    }
}
