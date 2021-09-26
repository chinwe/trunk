package chapter6;

/**
 * 第39条：注解优先于命名模式
 *
 *
 */
public class Item39 {

    @ExceptionTest(ArithmeticException.class)
    public static void divideByZero() {
        int i = 0;
        i = i / 0;
    }


}
