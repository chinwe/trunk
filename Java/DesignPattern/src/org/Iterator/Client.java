package org.Iterator;

import java.util.ArrayList;

/**
 * @author mozixun
 * @description
 * @date 2020/4/3 - 8:41 下午
 */
public class Client {

    public static void main(String[] args) {
        ArrayList<String> animals = new ArrayList<>();
        animals.add("Tiger");
        animals.add("Lion");

        Zoo zoo = new Zoo(animals);
        while (zoo.hasNext()) {
            System.out.println(zoo.next());
        }
    }
}
