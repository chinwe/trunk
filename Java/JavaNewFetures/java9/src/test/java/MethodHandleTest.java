import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 改进方法句柄（Method Handle）
 * 类 java.lang.invoke.MethodHandles 增加了更多的静态方法来创建不同类型的方法句柄。
 * arrayConstructor：创建指定类型的数组。
 * arrayLength：获取指定类型的数组的大小。
 * varHandleInvoker 和 varHandleExactInvoker：调用 VarHandle 中的访问模式方法。
 * zero：返回一个类型的默认值。
 * empty：返 回 MethodType 的返回值类型的默认值。
 * loop、countedLoop、iteratedLoop、whileLoop 和 doWhileLoop：创建不同类型的循环，包括 for 循环、while 循环 和 do-while 循环。
 * tryFinally：把对方法句柄的调用封装在 try-finally 语句中。
 *
 * @author chinwe
 * 2024/1/27
 */
class MethodHandleTest {
    static int body(final int sum, final String value) {
        return sum + value.length();
    }

    @Test
    void testIteratedLoop() throws Throwable {
        final MethodHandle iterator = MethodHandles.constant(
                Iterator.class,
                List.of("a", "bc", "def").iterator());
        final MethodHandle init = MethodHandles.zero(int.class);
        final MethodHandle body = MethodHandles
                .lookup()
                .findStatic(
                        MethodHandleTest.class,
                        "body",
                        MethodType.methodType(
                                int.class,
                                int.class,
                                String.class));
        final MethodHandle iteratedLoop = MethodHandles
                .iteratedLoop(iterator, init, body);
        assertEquals(6, iteratedLoop.invoke());
    }
}
