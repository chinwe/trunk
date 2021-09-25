package chapter5;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

/**
 * 第30条：优先考虑泛型方法
 *
 *
 */
public class Item30 {

    public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> result = new HashSet<>(s1);
        result.addAll(s2);
        return result;
    }

    public static void main(String[] args) {
        Set<String> guys = Sets.newHashSet("Tom", "Dick", "Harry");
        Set<String> stooges = Sets.newHashSet("Larry", "Moe", "Curly");
        Set<String> aflCio = union(guys, stooges);
        System.out.println(aflCio);
    }
}
