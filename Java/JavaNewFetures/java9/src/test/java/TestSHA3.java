import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Java 9 新增了 4 个 SHA-3 哈希算法，SHA3-224、SHA3-256、SHA3-384 和 SHA3-512。
 * 另外也增加了通过 java.security.SecureRandom 生成使用 DRBG 算法的强随机数。
 *
 * @author chinwe
 * 2024/1/27
 */
class TestSHA3 {
    @Test
    void testSHA3() throws NoSuchAlgorithmException {
        final MessageDigest instance = MessageDigest.getInstance("SHA3-224");
        final byte[] digest = instance.digest("".getBytes());
        assertEquals("6b4e03423667dbb73b6e15454f0eb1abd4597f9a1b078e3f5b5a6bc7", Hex.encodeHexString(digest));
    }
}
