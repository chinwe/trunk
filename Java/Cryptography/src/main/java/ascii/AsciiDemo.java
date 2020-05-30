package ascii;

public class AsciiDemo {
    public static void main(String[] args) {
        String a = "Aaz";
        char[] chars = a.toCharArray();
        for (char aChar : chars) {
            System.out.println((int)aChar);
        }
    }
}
