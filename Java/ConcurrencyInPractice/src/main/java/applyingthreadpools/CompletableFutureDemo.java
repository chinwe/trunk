package applyingthreadpools;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author chinwe
 * 2021/12/15
 */
public class CompletableFutureDemo {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(10, 20, 30, 40, 50);
        long count = list.parallelStream()
                .map(n -> CompletableFuture.supplyAsync(() -> getDataById(n)))
                .map(cf -> cf.thenApply(CompletableFutureDemo::sendData))
                .map(CompletableFuture::join).count();

        System.out.println("Number of elements:" + count);
    }

    private static String getDataById(int id) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("getDataById: "+ Thread.currentThread().getName());
        return "Data:"+ id;
    }

    private static String sendData(String data) {
        System.out.println("sendData: "+ Thread.currentThread().getName());
        System.out.println(data);
        return data;
    }
}
