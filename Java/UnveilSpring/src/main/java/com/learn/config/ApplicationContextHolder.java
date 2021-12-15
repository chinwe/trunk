package com.learn.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author chinwe
 * 2021/12/12
 */
@Component
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return appContext.getBean(requiredType);
    }
}
