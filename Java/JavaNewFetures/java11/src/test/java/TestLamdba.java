import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 在 Lambda 表达式中使用局部变量类型推断是 Java 11 引入的唯一与语言相关的特性
 *
 * @author chinwe
 * 2024/1/28
 */
class TestLamdba {

    @Test
    void testLambda() {
        final int value = Stream.of(1, 2, 3, 4, 5)
                .reduce(0, (var a, var b) -> a + b);
        assertEquals(15, value);
    }
}
