package org.example.corejava.inheritance;

/**
 * @author chinwe
 */
public class PersonTest {
    public static void main(String[] args) {
        Person[] people = new Person[2];
        people[0] = new Student("Maria Morris", "Computer science");
        people[1] = new Student("Maria Morris", "Computer science");

        for (Person person : people) {
            System.out.println(person.getName() + ", " + person.getDesc());
        }
    }
}
