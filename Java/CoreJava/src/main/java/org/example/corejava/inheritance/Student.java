package org.example.corejava.inheritance;


/**
 * @author chinwe
 */
public class Student extends Person {

    private String major;

    public Student(String name, String major) {
        super(name);
        this.major = major;
    }

    @Override
    public String getDesc() {
        return "a student majoring in " + major;
    }
}
