package chapter9;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * 第59条：了解和使用类库
 *
 * java.lang java.util java.io
 * Collections
 * Stream
 *
 */
public class Item59 {
    public static void main(String[] args) {
        // 现在选择随机数生成器时，大多使用ThreadLocalRandom
        final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        IntStream.range(0, 5).map(i -> threadLocalRandom.nextInt(0, 100)).forEach(System.out::println);
    }
}
