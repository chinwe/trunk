package com.learn.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * UserDaoProxy
 * @author chinwe
 * 2021/12/26
 */
public class UserDaoProxy implements InvocationHandler {

    private final Object target;

    public UserDaoProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        System.out.println("before invoke");

        final Object result = method.invoke(target, args);

        System.out.println("after invoke");

        return result;
    }
}