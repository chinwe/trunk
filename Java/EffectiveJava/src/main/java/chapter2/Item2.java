package chapter2;

import lombok.Builder;
import lombok.ToString;

/**
 * 第2条：遇到多个构造器参数时要考虑使用构建器
 * Builder
 *
 * 如果类的构造器或者静态工厂中具有多个参数，设计这种类时，Builder模式就是一种不错的选择
 *
 */
public class Item2 {

    @Builder
    @ToString
    private static class Device {
        String id;
        String name;
    }

    public static void main(String[] args) {
        Device device = Device.builder()
                .id("1")
                .name("device1")
                .build();

        System.out.println(device);
    }
}
