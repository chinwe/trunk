package org.Iterator;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author mozixun
 * @description
 * @date 2020/4/3 - 8:42 下午
 */
public class Zoo implements Iterator<String> {

    private ArrayList<String> animals = new ArrayList<>();
    private int index = 0;

    public Zoo(ArrayList<String> animals) {
        this.animals = animals;
    }

    @Override
    public boolean hasNext() {
        if (index < animals.size()) {
            index++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String next() {
        return animals.get(index - 1);
    }
}
