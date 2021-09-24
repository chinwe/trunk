package chapter2;

import java.io.*;

/**
 * 第9条：try-with-resources优于try-finally
 *
 * 在处理必须关闭的资源时，始终要要优先考虑用try-with-resources，而不是try-finally
 *
 */
public class Item9 {
    public static void main(String[] args) throws IOException {
        copy("C:/1.txt", "C:/2.txt");
    }

    public static final int BUFFER_SIZE = 1024;

    private static void copy(String src, String dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)
        ) {
            byte[] buf = new byte[BUFFER_SIZE];
            int n = 0;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
        }
    }
}
