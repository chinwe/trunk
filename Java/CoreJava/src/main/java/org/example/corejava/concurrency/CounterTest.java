package org.example.corejava.concurrency;

import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * @author chinwe
 * 2021/9/20
 */
public class CounterTest {

  private static Integer number = 0;

  private static final AtomicLong atomic_number = new AtomicLong();

 public static void main(String[] args) throws InterruptedException {

  threadSafeCount();
 }

 private static void threadSafeCount() throws InterruptedException {
  Runnable runnable = CounterTest::count;

  ExecutorService threadPoolExecutor = getExecutorService();

  threadPoolExecutor.execute(runnable);
  threadPoolExecutor.execute(runnable);
  threadPoolExecutor.execute(runnable);

  threadPoolExecutor.execute(CounterTest::atomCount);
  threadPoolExecutor.execute(CounterTest::atomCount);
  threadPoolExecutor.execute(CounterTest::atomCount);

  threadPoolExecutor.shutdown();
  threadPoolExecutor.awaitTermination(3, TimeUnit.SECONDS);

  System.out.println(number);
  System.out.println(atomic_number.get());
 }

 private static ExecutorService getExecutorService() {
  ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
          .setNamePrefix("demo-pool-")
          .build();
  return new ThreadPoolExecutor(1, 2,
          0L, TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
 }

 private static synchronized void count() {
  System.out.println("Thread " + Thread.currentThread().getName());
  IntStream.range(0, 1_000_000).forEach(i -> number += 1);
 }

 private static void atomCount() {
  System.out.println("Thread " + Thread.currentThread().getName());
  IntStream.range(0, 1_000_000).forEach(i -> atomic_number.incrementAndGet());
 }
}
