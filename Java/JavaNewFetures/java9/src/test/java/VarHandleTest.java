import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 变量句柄是一个变量或一组变量的引用，包括静态域，非静态域，数组元素和堆外数据结构中的组成部分等。
 * 变量句柄的含义类似于已有的方法句柄。变量句柄由 Java 类 java.lang.invoke.VarHandle 来表示。
 * 可以使用类 java.lang.invoke.MethodHandles.Lookup 中的静态工厂方法来创建 VarHandle 对象。
 * 通过变量句柄，可以在变量上进行各种操作。这些操作称为访问模式。不同的访问模式尤其在内存排序上的不同语义。
 *
 * @author chinwe
 * 2024/1/27
 */
class VarHandleTest {
    private HandleTarget handleTarget = new HandleTarget();
    private VarHandle varHandle;

    @BeforeEach
    public void setUp() throws Exception {
        this.handleTarget = new HandleTarget();
        this.varHandle = MethodHandles
                .lookup()
                .findVarHandle(HandleTarget.class, "count", int.class);
    }
    @Test
    void testGet() {
        assertEquals(1, this.varHandle.get(this.handleTarget));
        assertEquals(1, this.varHandle.getVolatile(this.handleTarget));
        assertEquals(1, this.varHandle.getOpaque(this.handleTarget));
        assertEquals(1, this.varHandle.getAcquire(this.handleTarget));
    }

    @Test
    void testSet() {
        this.varHandle.set(this.handleTarget, 2);
        assertEquals(2, this.varHandle.get(this.handleTarget));
    }

    private static class HandleTarget {
        public int count = 1;
    }
}
