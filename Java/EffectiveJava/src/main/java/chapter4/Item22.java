package chapter4;

import static chapter4.Item22.PhysicalConstants.DAY_OF_WEEK;

/**
 * 第22条：接口只用于定义类型
 *
 * 接口应该只被用来定义类型，不应该用来导出常量
 *
 */
public class Item22 {

    public static class PhysicalConstants {
        private PhysicalConstants() { }

        public static final int DAY_OF_WEEK = 7;
    }

    public static void main(String[] args) {
        System.out.println(DAY_OF_WEEK);
    }
}
