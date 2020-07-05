package stream;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class StreamMain {
    public static void main(String[] args) {
        List<Dish> menu = Arrays.asList(
                new Dish("pork", false, 800, Dish.Type.MEAT),
                new Dish("beef", false, 700, Dish.Type.MEAT),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER),
                new Dish("rice", true, 120, Dish.Type.OTHER),
                new Dish("pizza", true, 550, Dish.Type.OTHER),
                new Dish("prawns", false, 300, Dish.Type.FISH),
                new Dish("salmon", false, 450, Dish.Type.FISH)
        );

        List<String> threeHighCaloricDishNames = menu.stream()
                .sorted(Comparator.comparing(Dish::getCalories).reversed())
                .filter(d -> d.getCalories() > 300)
                .map(Dish::getName)
                .limit(3)
                .collect(toList());
        System.out.println(threeHighCaloricDishNames);

        List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
        numbers.stream()
                .filter(i -> i % 2 == 0)
                .distinct()
                .skip(1)
                .forEach(System.out::println);

        // 查找和匹配
        // allMatch、 anyMatch、 noneMatch、 findFirst、findAny

        if (menu.stream().anyMatch(Dish::isVegetarian)) {
            System.out.println("The menu is (somewhat) vegetarian friendly!!");
        }

        menu.stream()
            .filter(Dish::isVegetarian)
            .findFirst()
            .ifPresent(d -> System.out.println(d.getName()));

        // 归约
        int total = numbers.stream().reduce(0, Integer::sum);
        System.out.println(total);

        Optional<Integer> max = numbers.stream().reduce(Integer::max);
        System.out.println(max);

        // 收集器

        long howManyDishes = menu.stream().collect(Collectors.counting());

        Optional<Dish> mostCaloriesDish = menu.stream().collect(maxBy(Comparator.comparing(Dish::getCalories)));
        if (mostCaloriesDish.isPresent()) {
            System.out.println(mostCaloriesDish.toString());
        }

        int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));
        System.out.println("totalCalories: " + totalCalories);

        Map<Dish.Type, List<Dish>> dishesByType = menu.stream().collect(groupingBy(Dish::getType));
        System.out.println(dishesByType);
    }
}
