package org.example.corejava.oop;

import lombok.*;

import java.time.LocalDate;

/**
 * @author chinwe
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Employee {
    /**
     * ID
     */
    Integer id;

    /**
     * Name
     */
    String name;

    /**
     * Hire Date
     */
    private LocalDate hireDate;
}

