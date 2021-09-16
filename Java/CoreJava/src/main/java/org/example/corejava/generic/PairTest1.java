package org.example.corejava.generic;

import java.io.Serializable;

/**
 * @author chinwe
 * 2021/9/12
 */
public class PairTest1 {
    public static void main(String[] args) {
        Pair<String> stringPair = new Pair<>("first", "second");

        System.out.println(stringPair.getFirst());
        System.out.println(stringPair.getSecond());

        printBuddies(stringPair);
    }

    // 通配符类型
    public static void printBuddies(final Pair<? extends Serializable> p) {
        System.out.println(p.toString());
    }
}
