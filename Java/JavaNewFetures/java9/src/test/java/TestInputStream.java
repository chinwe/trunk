import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 类 java.io.InputStream 中增加了新的方法来读取和复制 InputStream 中包含的数据
 *
 * @author chinwe
 * 2024/1/27
 */
class TestInputStream {
    private InputStream inputStream;
    private static final String CONTENT = "Hello World";
    @BeforeEach
    public void setUp() throws Exception {
        this.inputStream =
                TestInputStream.class.getResourceAsStream("/input.txt");
    }
    @Test
    void testReadAllBytes() throws Exception {
        final String content = new String(this.inputStream.readAllBytes());
        assertEquals(CONTENT, content);
    }
    @Test
    void testReadNBytes() throws Exception {
        final byte[] data = new byte[5];
        this.inputStream.readNBytes(data, 0, 5);
        assertEquals("Hello", new String(data));
    }
    @Test
    void testTransferTo() throws Exception {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.inputStream.transferTo(outputStream);
        assertEquals(CONTENT, outputStream.toString());
    }
}