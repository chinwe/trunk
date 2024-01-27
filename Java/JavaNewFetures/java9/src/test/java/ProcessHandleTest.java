import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Java 9 增加了 ProcessHandle 接口，可以对原生进程进行管理，尤其适合于管理长时间运行的进程。
 *
 * @author chinwe
 * 2024/1/27
 */
class ProcessHandleTest {

    @Test
    void testProcessHandle() throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder("java", "-version")
                .inheritIO();
        final ProcessHandle processHandle = processBuilder.start().toHandle();
        processHandle.onExit().whenCompleteAsync((handle, throwable) -> {
            if (throwable == null) {
                System.out.println(handle.pid());
            } else {
                throwable.printStackTrace();
            }
        });
    }
}
