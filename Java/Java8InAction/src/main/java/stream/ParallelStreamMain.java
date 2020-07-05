package stream;

import java.util.stream.LongStream;

public class ParallelStreamMain {
    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        long sum = parallelSum(1_000_000);
        System.out.println(sum);
        long duration = System.currentTimeMillis() - start;
        System.out.println("duration: " + duration);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("availableProcessors: " + availableProcessors);
    }

    public static long parallelSum(long n) {
        return LongStream.rangeClosed(1, n)
                .parallel()
                .reduce(0L, Long::sum);
    }
}
