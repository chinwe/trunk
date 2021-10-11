package sharingobjects;

/**
 * 在没有同步的情况下共享变量
 *
 * 只要数据需要被跨线程共享，就进行恰当的同步
 * 加锁可以保证可见性与原子性；volatile变量只能保证可见性
 *
 * @author chinwe
 * 2021/10/1
 */
public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread extends Thread {
        @Override
        public void run() {
            while (!ready) {
                Thread.yield();
            }
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        new ReaderThread().start();
        number = 42;
        ready = true;
    }
}
