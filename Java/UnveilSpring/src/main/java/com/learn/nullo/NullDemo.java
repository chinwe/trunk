package com.learn.nullo;

import org.springframework.lang.Nullable;

/**
 * @author chinwe
 * 2022/1/16
 */
public class NullDemo {
    public static void main(String[] args) {
        Integer value = getData();
        System.out.println(value);
    }

    @Nullable
    private static Integer getData() {
        return null;
    }
}
