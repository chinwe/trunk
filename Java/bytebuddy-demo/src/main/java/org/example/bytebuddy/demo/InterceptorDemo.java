package org.example.bytebuddy.demo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author chinwe
 * 2025/2/16
 */
public class InterceptorDemo {

    public static class Interceptor {
        public static String intercept() {
            return "Intercepted!";
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try (var byteBuddy = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.named("toString"))
                .intercept(MethodDelegation.to(Interceptor.class))
                .make()) {

            Class<?> dynamicType = byteBuddy.load(Interceptor.class.getClassLoader()).getLoaded();

            Constructor<?> constructor = dynamicType.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            System.out.println(instance);
        }
    }
}
