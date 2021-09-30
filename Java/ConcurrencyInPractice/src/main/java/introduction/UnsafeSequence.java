package introduction;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * 非线程安全的序列
 * @author chinwe
 */
@NotThreadSafe
public class UnsafeSequence {
    private int value;

    public int getNext() {
        return value++;
    }
}
