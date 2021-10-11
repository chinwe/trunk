package composingobjects;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Java monitor pattern
 *
 * @author chinwe
 * 2021/10/10
 */
@ThreadSafe
public final class Counter {
    @GuardedBy("this")
    private long value = 0;

    public synchronized long getValue() {
        return value;
    }

    public synchronized long increment() {
        if (Long.MAX_VALUE == value) {
            throw new IllegalStateException("counter overflow");
        }
        return ++value;
    }
}
