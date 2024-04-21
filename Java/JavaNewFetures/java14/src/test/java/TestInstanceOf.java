import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author chinwe
 * 2024/4/21
 */
public class TestInstanceOf {

    @Test
    void testInstanceOf() {
        Object obj = "Hello";
        assertTrue(obj instanceof String str && str.length() > 4);

    }
}
