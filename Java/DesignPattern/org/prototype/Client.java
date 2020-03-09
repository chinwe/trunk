package org.prototype;

public class Client {
    
    public static void main(String[] args) {

        Sheep sheep = new Sheep("Dolly", 1, "White");
        Object sheep2 = sheep.clone();

        System.out.println(sheep);
        System.out.println(sheep2);
    }
}
