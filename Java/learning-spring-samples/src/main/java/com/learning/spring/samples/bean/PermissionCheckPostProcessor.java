package com.learning.spring.samples.bean;

import com.learning.spring.samples.annotation.PermissionCheck;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

// @Component
public class PermissionCheckPostProcessor implements BeanPostProcessor {

   @Override
   public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
       Class<?> beanClass = bean.getClass();
       if (beanClass.isAnnotationPresent(RestController.class)) {
           Method[] methods = beanClass.getDeclaredMethods();
           for (Method method : methods) {
               if (method.isAnnotationPresent(RequestMapping.class)
                       || method.isAnnotationPresent(GetMapping.class)
                       || method.isAnnotationPresent(PostMapping.class)) {
                   if (!method.isAnnotationPresent(PermissionCheck.class)) {
                       throw new IllegalStateException("Method " + method.getName() + " in Controller " + beanClass.getName() + " is missing @PermissionCheck");
                   }
               }
           }
       }
       return bean;
   }

   @Override
   public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
       return bean;
   }
}
