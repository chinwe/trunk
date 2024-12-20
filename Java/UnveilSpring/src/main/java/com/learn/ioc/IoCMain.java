package com.learn.ioc;

import com.learn.config.AppConfig;
import com.learn.config.ApplicationContextHolder;
import com.learn.service.ActionRouteService;
import com.learn.service.MultiInstance;
import com.learn.service.UserService;
import com.learn.service.impl.ActionOne;
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

        final MultiInstance instance1 = ApplicationContextHolder.getBean(MultiInstance.class);
        final MultiInstance instance2 = ApplicationContextHolder.getBean(MultiInstance.class);
        System.out.println(instance1);
        System.out.println(instance2);

        final ActionRouteService actionRouteService = ApplicationContextHolder.getBean(ActionRouteService.class);
        actionRouteService.doAction(ActionOne.class.getName());
    }
}
