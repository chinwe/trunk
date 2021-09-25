package chapter3;

import lombok.ToString;

/**
 * 第12条：始终要覆盖toString
 *
 * 在实际应用中，toString方法应该返回对象中包含的所有值得关注的信息
 *
 */
public class Item12 {

    @ToString
    static class Person {
        String name;
        Integer age;
    }

    public static void main(String[] args) {
        Item12.Person person = new Item12.Person();
        System.out.println(person);
    }
}
