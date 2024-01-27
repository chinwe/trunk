import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 在集合上，Java 9 增加 了 List.of()、Set.of()、Map.of() 和 Map.ofEntries()等工厂方法来创建不可变集合。
 *
 * @author chinwe
 * 2024/1/26
 */
class CollectionTest {
    @Test
    void testCollectionOf() {
        System.out.println(List.of());
        System.out.println(List.of("Hello", "World"));
        System.out.println(List.of(1, 2, 3));

        System.out.println(Set.of());
        System.out.println(Set.of("Hello", "World"));
        System.out.println(Set.of(1, 2, 3));

        System.out.println(Map.of());
        System.out.println(Map.of("Hello", 1, "World", 2));

        // 对不可变集合操作会抛出异常 java.lang.UnsupportedOperationException
    }
}
