package org.example.corejava.basic;

import java.io.Console;
import java.util.Scanner;

/**
 * 输入
 * @author chinwe
 */
public class Input {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("What is your name?");
        String name = scanner.nextLine();

        System.out.println("How old are you?");
        int age = scanner.nextInt();

        System.out.println("Hello, " + name + ". Next year, you'll be " + (age + 1) + ".");

        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword("Password: ");
        } else {
            System.out.println("console is null.");
        }
    }
}
