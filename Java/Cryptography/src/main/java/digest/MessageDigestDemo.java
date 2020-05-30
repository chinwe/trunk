package digest;

import java.security.MessageDigest;

public class MessageDigestDemo {
    public static void main(String[] args) throws Exception {

        String input = "test message digest.";
        String algorithm = "SHA-256";

        MessageDigest md = MessageDigest.getInstance(algorithm);

        byte[] bytes = md.digest(input.getBytes());
        String ciphertext = byteToHex(bytes);
        System.out.println(ciphertext);
    }

    public static String byteToHex(byte[] bytes){
        String hex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            hex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((hex.length() == 1) ? "0" + hex : hex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }
}

