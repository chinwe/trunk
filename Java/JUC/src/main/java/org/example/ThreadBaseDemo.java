package org.example;

/**
 * @author mozixun
 * @description
 * @date 2023/3/12 - 22:00
 */
public class ThreadBaseDemo {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            System.out.println("t1");
        }, "t1");
        thread.start();
    }
}
