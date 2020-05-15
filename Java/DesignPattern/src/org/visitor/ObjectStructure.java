package org.visitor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author mozixun
 * @description
 * @date 2020/4/2 - 10:29 下午
 */
public class ObjectStructure {

    private List<BasePerson> personList = new LinkedList<>();

    public void add(BasePerson basePerson) {
        personList.add(basePerson);
    }

    public void remove(BasePerson basePerson) {
        personList.remove(basePerson);
    }

    public void display(BaseAction baseAction) {
        for (BasePerson basePerson : personList) {
            basePerson.accept(baseAction);
        }
    }
}
