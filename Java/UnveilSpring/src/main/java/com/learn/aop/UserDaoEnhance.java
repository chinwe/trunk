package com.learn.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author chinwe
 * 2021/12/26
 */
@Component
@Aspect
public class UserDaoEnhance {

    @Before(value = "execution(* com.learn.aop.impl.UserDaoImpl.add(..))")
    public void before() {
        System.out.println("before add");
    }
}
