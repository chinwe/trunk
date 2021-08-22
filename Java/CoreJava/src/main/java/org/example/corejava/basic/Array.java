package org.example.corejava.basic;


import java.util.Arrays;

/**
 * 数组
 * @author chinwe
 */
public class Array {
    public static void main(String[] args) {
        int[] a = new int[100];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }

        // 字符串
        System.out.println(Arrays.toString(a));

        // 拷贝
        int[] b = Arrays.copyOf(a, 10);
        System.out.println(Arrays.toString(b));

        // 排序
        Arrays.sort(b);
    }
}
