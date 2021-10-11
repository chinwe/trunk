package buildingblocks;

import java.util.concurrent.CountDownLatch;

/**
 *
 * semaphore barrier latch
 *
 * @author chinwe
 * 2021/10/11
 */
public class Synchronizer {

    public static void main(String[] args) throws InterruptedException {
        testHareness();
    }

    private static void testHareness() throws InterruptedException {
        TestHarness testHarness = new TestHarness();
        final long timeTasks = testHarness.timeTasks(4, () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        });
        System.out.println(timeTasks);
    }

    public static class TestHarness {
        public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
            final CountDownLatch startGate = new CountDownLatch(1);
            final CountDownLatch endGate = new CountDownLatch(nThreads);
            for (int i = 0; i < nThreads; i++) {
                Thread t = new Thread(() -> {
                    try {
                        startGate.await();
                        try {
                            task.run();
                        } finally {
                            endGate.countDown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                });
                t.start();
            }
            long start = System.currentTimeMillis();
            startGate.countDown();
            endGate.await();
            long end = System.currentTimeMillis();
            return end - start;
        }
    }
}
