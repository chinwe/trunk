import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author chinwe
 * 2024/1/27
 */
class ConcurrentTest {

    @Test
    void testCompletableFuture() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello";
        }).completeOnTimeout("timeout", 500, TimeUnit.MILLISECONDS);

        assertEquals("timeout", future.join());
    }
}

