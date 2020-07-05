package functional;

import java.util.function.DoubleUnaryOperator;

public class FunctionalProgramming {
    public static void main(String[] args) {

        // 高阶函数：接受至少一个函数作为参宿；返回的结果是一个函数

        DoubleUnaryOperator convertCtoF = curriedConverter(9.0 / 5, 32);
        System.out.println(convertCtoF.applyAsDouble(27));
    }

    static DoubleUnaryOperator curriedConverter(double f, double b) {
        return (double x) -> x * f + b;
    }
}
