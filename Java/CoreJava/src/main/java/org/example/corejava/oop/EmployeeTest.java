package org.example.corejava.oop;

import java.time.LocalDate;

/**
 * @author chinwe
 */
public class EmployeeTest {
    public static void main(String[] args) {
        Employee[] staff = new Employee[3];

        staff[0] = Employee.builder()
                .id(1)
                .name("San Zhang")
                .hireDate(LocalDate.of(1987, 12 ,15))
                .build();
        staff[1] = Employee.builder()
                .id(2)
                .name("Si Li")
                .hireDate(LocalDate.of(1989, 10 ,1))
                .build();
        staff[2] = Employee.builder()
                .id(3)
                .name("Wu Wang")
                .hireDate(LocalDate.of(1990, 3 ,15))
                .build();

        for (Employee employee : staff) {
            System.out.println(employee.toString());
        }
    }
}
