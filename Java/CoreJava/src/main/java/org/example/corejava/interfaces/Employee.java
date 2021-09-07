package org.example.corejava.interfaces;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @author chinwe
 */
@Data
@AllArgsConstructor
@ToString
public class Employee implements Comparable<Employee> {

    private String name;

    private double salary;

    @Override
    public int compareTo(Employee o) {
        return Double.compare(salary, o.salary);
    }
}
