import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 在 Java 12 中引入了 Switch 表达式作为预览特性
 *
 * @author chinwe
 * 2024/1/30
 */
class TestSwitch {
    private static String getText(int number) {
        return switch (number) {
            case 1, 2 -> "one or two";
            case 3 -> "three";
            case 4, 5, 6 -> "four or five or six";
            default -> "unknown";
        };
    }

    @Test
    void testSwitch() {
        assertEquals("one or two", getText(1));
    }
}
