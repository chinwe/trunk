package com.learn.aop;

import com.learn.aop.impl.UserDaoImpl;

import java.lang.reflect.Proxy;

/**
 * JDKProxy
 * @author chinwe
 * 2021/12/26
 */
public class JDKProxy {
    public static void main(String[] args) {
        UserDaoImpl userDaoImpl = new UserDaoImpl();

        Class<?>[] interfaces = new Class[]{ IUserDao.class };
        final IUserDao userDao = (IUserDao) Proxy.newProxyInstance(JDKProxy.class.getClassLoader(), interfaces, new UserDaoProxy(userDaoImpl));
        userDao.add(null);
    }
}
