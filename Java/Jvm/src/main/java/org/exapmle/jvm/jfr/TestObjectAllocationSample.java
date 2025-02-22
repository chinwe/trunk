package org.exapmle.jvm.jfr;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import jdk.test.whitebox.WhiteBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * VM options: -Xbootclasspath/a:lib/whitebox-1.0-SNAPSHOT.jar -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI
 */
public class TestObjectAllocationSample {
    //对于字节数组对象头占用16字节
    private static final int BYTE_ARRAY_OVERHEAD = 16;
    //分配对象的大小，1MB
    private static final int OBJECT_SIZE = 1024 * 1024;
    //要分配的对象个数
    private static final int OBJECTS_TO_ALLOCATE = 20;
    //分配对象的 class 名称
    private static final String BYTE_ARRAY_CLASS_NAME = byte[].class.getName();
    private static final String INT_ARRAY_CLASS_NAME = int[].class.getName();
    //测试的 JFR 事件名称
    private static final String EVENT_NAME = "jdk.ObjectAllocationSample";
    //分配的对象放入这个静态变量，防止编译器优化去掉没有使用的分配代码
    public static byte[] tmp;
    public static int[] tmp2;

    public static void main(String[] args) throws IOException, InterruptedException {
        //使用 WhiteBox 执行 FullGC，清除干扰
        WhiteBox whiteBox = WhiteBox.getWhiteBox();
        whiteBox.fullGC();

        try (Recording recording = new Recording()) {
            //设置 throttle 为 5/s，也就是每秒最多采集5个
            //目前 throttle 只对 jdk.ObjectAllocationSample 有效，还不算是标准配置，所以只能这样配置
            recording.enable(EVENT_NAME).with("throttle", "5/s");
            recording.start();
            //main 线程分配对象
            for (int i = 0; i < OBJECTS_TO_ALLOCATE; ++i) {
                //由于 main 线程在 JVM 初始化的时候分配了一些其他对象，所以第一次采集的大小可能不准确，或者采集的类不对，后面结果中我们会看到
                tmp = new byte[OBJECT_SIZE - BYTE_ARRAY_OVERHEAD];
                TimeUnit.MILLISECONDS.sleep(100);
            }
            //测试多线程分配对象
            Runnable runnable = () -> {
                for (int i = 0; i < OBJECTS_TO_ALLOCATE; ++i) {
                    tmp = new byte[OBJECT_SIZE - BYTE_ARRAY_OVERHEAD];
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread thread = new Thread(runnable);
            Runnable runnable2 = () -> {
                for (int i = 0; i < OBJECTS_TO_ALLOCATE; ++i) {
                    tmp2 = new int[OBJECT_SIZE - BYTE_ARRAY_OVERHEAD];
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread thread2 = new Thread(runnable2);
            thread.start();
            thread2.start();
            long threadId = thread.threadId();
            long threadId2 = thread2.threadId();
            thread.join();
            thread2.join();
            recording.stop();
            Path path = new File(new File(".").getAbsolutePath(), "recording-" + recording.getId() + "-pid" + ProcessHandle.current().pid() + ".jfr").toPath();
            recording.dump(path);

            for (RecordedEvent event : RecordingFile.readAllEvents(path)) {
                if (!EVENT_NAME.equals(event.getEventType().getName())) {
                    continue;
                }
                String objectClassName = event.getString("objectClass.name");
                boolean isMyEvent = (
                        Thread.currentThread().threadId() == event.getThread().getJavaThreadId()
                                || threadId == event.getThread().getJavaThreadId()
                                || threadId2 == event.getThread().getJavaThreadId()
                ) && (
                        objectClassName.equals(BYTE_ARRAY_CLASS_NAME) ||
                                objectClassName.equals(INT_ARRAY_CLASS_NAME)
                );
                if (!isMyEvent) {
                    continue;
                }
                System.out.println(event);
            }
        }
    }
}