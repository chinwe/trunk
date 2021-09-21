package org.example.corejava.stream;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author chinwe
 * 2021/9/20
 */
public class StreamMain {
    public static void main(String[] args) {
        // filter
        System.out.println("filter:");
        IntStream.range(1, 5).filter(i -> (i % 2) == 0).forEach(System.out::println);
        // map
        System.out.println("map:");
        IntStream.range(1, 5).map(i -> i * i).forEach(System.out::println);
        // flatMap
        System.out.println("flatMap:");
        IntStream.range(1, 5).flatMap(i -> IntStream.of(i * 2)).forEach(System.out::println);

        // limit        skip        concat

        // distinct     sorted      peek

        //  max         min
        //  findFirst   findAny
        //  anyMatch    allMatch    noneMatch

        //  forEach     collect
        final Stream<Locale> localeStream = Arrays.stream(Locale.getAvailableLocales());
        final Map<String, Set<String>> collect = localeStream.collect(Collectors.toMap(Locale::getDisplayCountry,
                locale -> Collections.singleton(locale.getDisplayLanguage()),
                (a, b) -> {
                    final HashSet<String> union = new HashSet<>(a);
                    union.addAll(b);
                    return union;
                }));
        System.out.println(collect);

        // groupingBy   partitioningBy

        // reduce
        System.out.println(IntStream.range(1, 5).map(i -> i * i).reduce((left, right) -> left + right));
    }
}
