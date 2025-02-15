package org.exapmle.jvm.jfr;

import jdk.test.whitebox.WhiteBox;

/**
 * VM Args: -Xbootclasspath/a:lib/whitebox-1.0-SNAPSHOT.jar -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xlog:gc
 *
 * @author chinwe
 * 2025/2/15
 */
public class TestWhiteBox {
    public static void main(String[] args) throws Exception {
        WhiteBox whiteBox = WhiteBox.getWhiteBox();
        //获取 ReservedCodeCacheSize 这个 JVM flag 的值
        Long reservedCodeCacheSize = whiteBox.getUintxVMFlag("ReservedCodeCacheSize");
        System.out.println(reservedCodeCacheSize);
        //打印堆内存各项指标
        whiteBox.printHeapSizes();
        //执行full GC
        whiteBox.fullGC();

        //保持进程不退出，保证日志打印完整
        Thread.currentThread().join();
    }
}
