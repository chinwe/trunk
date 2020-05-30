package rsa;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSADemo {
    public static void main(String[] args) throws Exception {
        // 生成密钥对
        String algorithm = "RSA";
        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance(algorithm);
        KeyPair keyPair = pairGenerator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] publicKeyEncoded = publicKey.getEncoded();
        byte[] privateKeyEncoded = privateKey.getEncoded();
        String pubKey = Base64.encode(publicKeyEncoded);
        // System.out.println(pubKey);
        String priKey = Base64.encode(privateKeyEncoded);
        // System.out.println(priKey);

        String plaintext = "test rsa";

        // 私钥加密
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] bytes = cipher.doFinal(plaintext.getBytes());
        System.out.println(Base64.encode(bytes));

        // 公钥解密
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] plantextBytes = cipher.doFinal(bytes);
        System.out.println(new String(plantextBytes));
    }
}
