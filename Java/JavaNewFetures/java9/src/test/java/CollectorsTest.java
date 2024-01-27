import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Collectors 中增加了新的方法 filtering 和 flatMapping
 *
 * @author chinwe
 * 2024/1/27
 */
class CollectorsTest {

    @Test
    void testFlatMapping() {
        final Set<Integer> result = Stream.of("a", "ab", "abc")
                .collect(Collectors.flatMapping(v -> v.chars().boxed(),
                        Collectors.toSet()));
        assertEquals(3, result.size());
    }

    @Test
    void testFiltering() {
        final Set<String> result = Stream.of("a", "ab", "abc")
                .collect(Collectors.filtering(v -> v.length() > 1,
                        Collectors.toSet()));
        assertEquals(2, result.size());
    }
}
