package aes;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESDemo {

    public static void main(String[] args) throws Exception {

        String alg = "AES/CBC/PKCS5Padding";
        String key = "1234567890!@#$%^";
        String iv  = "qwertyuiopasdfgh";

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());

        Cipher cipher = Cipher.getInstance(alg);

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        String plaintext = "test aes";

        byte[] bytes = cipher.doFinal(plaintext.getBytes());

        String ciphertext = Base64.encode(bytes);
        System.out.println(ciphertext);
    }
}
