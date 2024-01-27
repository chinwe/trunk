import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Stream 中增加了新的方法 ofNullable、dropWhile、takeWhile 和 iterate
 *
 * @author chinwe
 * 2024/1/26
 */
class StreamTest {

    @Test
    void testOfNullable() {
        final long count = Stream.ofNullable(null).count();
        assertEquals(0, count);
    }

    @Test
    void testDropWhile() {
        final long count = Stream.of(1, 2, 3, 4, 5)
                .dropWhile(i -> i % 2 != 0)
                .count();
        assertEquals(4, count);
    }

    @Test
    void testTakeWhile() {
        Stream.of(1, 2, 3, 4, 5)
                .takeWhile(i -> i < 3)
                .forEach(System.out::println);
    }

    @Test
    void testIterate() {
        Stream.iterate(1, i -> i + 1)
                .limit(5)
                .forEach(System.out::println);
        // new
        Stream.iterate(1, i -> i < 5, i -> i + 1)
                .forEach(System.out::println);
    }

}
