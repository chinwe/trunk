package introduction;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chinwe
 * 2021/9/30
 */
public class Main {
    private static final int LOOP = 1000;

    public static void main(String[] args) throws InterruptedException {
        sequence();
    }

    private static void sequence() throws InterruptedException {
        final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2, new BasicThreadFactory.Builder()
                .namingPattern("example-schedule-pool-%d")
                .daemon(true)
                .build());

        final UnsafeSequence unsafeSequence = new UnsafeSequence();
        final Sequence sequence = new Sequence();
        Runnable runnable = () -> {
            for (int i = 0; i < LOOP; i++) {
                unsafeSequence.getNext();
                sequence.getNext();
            }
        };
        executorService.submit(runnable);
        executorService.submit(runnable);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println(unsafeSequence.getNext());
        System.out.println(sequence.getNext());
    }
}
