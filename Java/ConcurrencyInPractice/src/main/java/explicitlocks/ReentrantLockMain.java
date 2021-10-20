package explicitlocks;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chinwe
 * 2021/10/17
 */
public class ReentrantLockMain {
    public static void main(String[] args) {
        ReentrantLock reentrantLock = new ReentrantLock();

        new Thread(() -> reentrantLock.lock()).start();

        // dead lock
        reentrantLock.lock();

        // ReadWriteLock readWriteLock;

    }
}
