package org.example.corejava.interfaces;

import java.util.Arrays;

/**
 * @author chinwe
 */
public class EmployeeSort {
    public static void main(String[] args) {
        Employee[] employees = new Employee[3];
        employees[0] = new Employee("Harry Hacker", 35000);
        employees[1] = new Employee("Carl Hacker", 75000);
        employees[2] = new Employee("Tony Hacker", 38000);

        Arrays.sort(employees);

        // lambda
        Arrays.sort(employees, (l, r) -> (int)(l.getSalary() - r.getSalary()));

        for (Employee employee : employees) {
            System.out.println(employee);
        }
    }
}
