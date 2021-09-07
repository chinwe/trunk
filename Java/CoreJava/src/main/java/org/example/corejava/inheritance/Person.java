package org.example.corejava.inheritance;

/**
 * @author chinwe
 */
public abstract class Person {
    /**
     * Name
     */
    private String name;

    public Person(String name) {
        this.name = name;
    }

    /**
     * 获取描述信息
     * @return 描述信息
     */
    public abstract String getDesc();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
