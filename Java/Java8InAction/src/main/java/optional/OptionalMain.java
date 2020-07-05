package optional;

import java.util.Optional;

public class OptionalMain {
    public static void main(String[] args) {
        Optional<Integer> optInt = stringToInt("1234");
        optInt.ifPresent(System.out::println);
    }

    public static Optional<Integer> stringToInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
