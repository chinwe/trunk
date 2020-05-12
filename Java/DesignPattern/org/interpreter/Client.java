package org.interpreter;

import java.util.HashMap;

public class Client {
    public static void main(String[] args) {

        HashMap<String, Integer> var = new HashMap<>();
        var.put("a", 10);
        var.put("b", 20);
        var.put("c", 30);
        var.put("d", 40);

        Calculator calculator = new Calculator();
        System.out.println(calculator.run(var));
    }
}
