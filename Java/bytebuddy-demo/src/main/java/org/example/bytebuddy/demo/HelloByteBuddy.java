package org.example.bytebuddy.demo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 生成一个简单类
 *
 * @author chinwe
 * 2025/2/16
 */
public class HelloByteBuddy {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        try (var byteBuddy = new ByteBuddy()
                .subclass(Object.class)
                .name("org.example.bytebuddy.demo.Hello")
                .method(ElementMatchers.named("toString"))
                .intercept(FixedValue.value("Hello ByteBuddy!"))
                .make()) {

            Class<?> dynamicType = byteBuddy.load(HelloByteBuddy.class.getClassLoader()).getLoaded();

            Constructor<?> constructor = dynamicType.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            System.out.println(instance);
        }
    }
}
