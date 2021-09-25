package chapter3;

/**
 * 第11条：覆盖equals时总要覆盖hashCode
 *
 */
public class Item11 {
    static class Person {
        String name;
        Integer age;
    }

    public static void main(String[] args) {
        Person person = new Person();
        System.out.println(person.hashCode());
    }
}


