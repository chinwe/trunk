package org.example.corejava.io;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;

/**
 * @author chinwe
 * 2021/9/21
 */
public class IOMain {

    public static void main(String[] args) throws IOException {
        System.out.print("checksum: ");
        final long begin = System.currentTimeMillis();
        final long checksum = checksumMappedFile(Paths.get("D:\\Program Files\\java-se-8u41-ri\\jre\\lib\\rt.jar"));
        System.out.println(Long.toHexString(checksum));
        final long end = System.currentTimeMillis();
        System.out.println((end - begin) + "ms");
    }

    // 内存文件映射
    public static final long checksumMappedFile(Path path) throws IOException {
        try(final FileChannel channel = FileChannel.open(path)) {
            CRC32 crc32 = new CRC32();
            int length = (int)channel.size();
            final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);

            for (int i = 0; i < length; i++) {
                crc32.update(buffer.get(i));
            }
            return crc32.getValue();
        }
    }
}
