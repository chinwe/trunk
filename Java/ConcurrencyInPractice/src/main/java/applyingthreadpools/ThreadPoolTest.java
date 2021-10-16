package applyingthreadpools;

/**
 *
 *
 * 线程池大小
 * NThreads = NCPU * UCPU * (1 + W / C)
 * NCPU CPU核数
 * UCPU 目标CPU的使用率[0.0, 1.0]
 * W/C 等待时间与计算时间的比率
 *
 * @author chinwe
 * 2021/10/14
 */
public class ThreadPoolTest {
 public static void main(String[] args) {
  // 获取CPU核数
  int N_CPUS = Runtime.getRuntime().availableProcessors();
  System.out.println(N_CPUS);
 }
}
