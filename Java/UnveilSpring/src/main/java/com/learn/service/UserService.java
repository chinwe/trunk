package com.learn.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chinwe
 * 2021/11/14
 */
@Service
public class UserService implements BeanPostProcessor {

    @Resource
    IndexService indexService;

    public UserService() {
        System.out.println("UserService Construct");
    }

    public void getService() {
        System.out.println(indexService);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
