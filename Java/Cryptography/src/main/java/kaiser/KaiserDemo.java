package kaiser;

public class KaiserDemo {

    public static void main(String[] args) {
        String plaintext = "Hello world";
        int key = 3;
        String ciphertext = encryptKaiser(plaintext, key);
        System.out.println(ciphertext);

        plaintext = decryptKaiser(ciphertext, key);
        System.out.println(plaintext);
    }

    private static String encryptKaiser(String plaintext, int key) {
        char[] charArray = plaintext.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            int b = charArray[i] + key;
            builder.append((char)b);
        }
        return builder.toString();
    }

    private static String decryptKaiser(String ciphertext, int key) {
        char[] charArray = ciphertext.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            int b = charArray[i] - key;

            builder.append((char)b);
        }
        return builder.toString();
    }
}
