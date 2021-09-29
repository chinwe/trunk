package chapter11;

import java.util.concurrent.TimeUnit;

/**
 * 第78条：同步访问共享的可变数据
 *
 * Java语言规范保证读或者写一个变量是原子的，除非这个变量的类型为long或者double
 * 为了在线程之间进行可靠的通信，也为了互斥访问，同步是必要的
 * 当多个线程共享可变数据的时候，每个读或者写数据的线程都必须执行同步
 *
 */
public class Item78 {

    private static boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {
        stopThread();
    }

    private static void stopThread() throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested)
                i++;
        });
        backgroundThread.start();

        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;

        backgroundThread.join();
    }
}
