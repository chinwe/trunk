package org.example.corejava.basic;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author mozixun
 * @description
 * @date 2021/11/27 - 8:14 下午
 */
public class Raii {
    public static void main(String[] args) {
        try (Defer defer = new Defer(() -> System.out.println("add log"))) {
            System.out.println("do some thing");
        }
    }

    public static class Defer implements AutoCloseable {

        private Runnable runnable;

        public Defer(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void close() {
            if (this.runnable != null) {
                this.runnable.run();
            }
        }
    }
}
