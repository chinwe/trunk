package org.visitor;

/**
 * @author mozixun
 * @description
 * @date 2020/4/2 - 10:18 下午
 */
public class Client {

    public static void main(String[] args) {

        ObjectStructure objectStructure = new ObjectStructure();
        objectStructure.add(new Man());
        objectStructure.add(new Woman());

        Success success = new Success();
        objectStructure.display(success);

    }
}
