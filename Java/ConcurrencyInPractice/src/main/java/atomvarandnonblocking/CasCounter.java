package atomvarandnonblocking;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chinwe
 * 2021/10/20
 */
public class CasCounter {
    private AtomicInteger value;

    public int getValue() {
        return value.get();
    }

    public int increment() {
        int v;
        do {
            v = value.get();
        } while (!value.compareAndSet(v, v + 1));
        return v + 1;
    }
}
