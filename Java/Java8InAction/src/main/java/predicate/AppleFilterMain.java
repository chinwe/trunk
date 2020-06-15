package predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppleFilterMain {

    public static void main(String[] args) {
        List<Apple> appleList = Arrays.asList(new Apple("red", 80),
                new Apple("green", 90));

        List<Apple> result = filterApples(appleList, apple -> "red".equals(apple.getColor()));
        for (Apple apple : result) {
            System.out.println(apple.getColor());
        }
    }

    private static List<Apple> filterApples(List<Apple> inventory, IApplePredicate applePredicate) {
        List<Apple> result = new ArrayList<Apple>();
        for (Apple apple : inventory) {
            if (applePredicate.test(apple)) {
                result.add(apple);
            }
        }
        return result;
    }
}
