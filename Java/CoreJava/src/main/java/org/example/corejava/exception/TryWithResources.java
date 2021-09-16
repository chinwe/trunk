package org.example.corejava.exception;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author chinwe
 */
public class TryWithResources {
    public static void main(String[] args) {
        try (final Scanner scanner = new Scanner(new FileInputStream("1.txt"), "UTF-8")) {
            while (scanner.hasNext()) {
                System.out.println(scanner.next());
            }
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
