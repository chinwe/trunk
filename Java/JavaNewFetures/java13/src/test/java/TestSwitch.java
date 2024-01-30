import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 在 Java 13 之后，Switch 表达式中就多了一个关键字用于跳出 Switch 块的关键字 yield，
 * 主要用于返回一个值，它和 return 的区别在于：return 会直接跳出当前循环或者方法，
 * 而 yield 只会跳出当前 Switch块，同时在使用 yield 时，需要有 default 条件。
 *
 * @author chinwe
 * 2024/1/30
 */
class TestSwitch {

    private static String getText(int number) {
        return switch (number) {
            case 1, 2:
                yield "one or two";
            case 3:
                yield "three";
            case 4, 5, 6:
                yield "four or five or six";
            default:
                yield "unknown";
        };
    }

    @Test
    void testSwitch() {
        assertEquals("one or two", getText(1));
        assertEquals("unknown", getText(7));
    }

}
