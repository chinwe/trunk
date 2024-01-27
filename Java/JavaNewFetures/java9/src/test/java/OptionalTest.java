import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Optional 类中新增了 ifPresentOrElse、or 和 stream 等方法。
 *
 * @author chinwe
 * 2024/1/27
 */
class OptionalTest {

    @Test
    void testStream() {
        final long count = Stream.of(
                        Optional.of(1),
                        Optional.empty(),
                        Optional.of(2),
                        Optional.empty().or(() -> Optional.of(3))
                ).flatMap(Optional::stream)
                .count();
        assertEquals(3, count);
    }
}
