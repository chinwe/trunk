import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 局部变量类型推断是 Java 10 中最值得开发人员注意的新特性，这是 Java 语言开发人员为了简化 Java 应用程序的编写而进行的又一重要改进。
 *
 * @author chinwe
 * 2024/1/28
 */
class TestVar {

    @Test
    void testVar() {
        var list = new ArrayList<String>(); // ArrayList<String>
        assertEquals("ArrayList", list.getClass().getSimpleName());

        var stream = list.stream(); // Stream<String>

    }
}
