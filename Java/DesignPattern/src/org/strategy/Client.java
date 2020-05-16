package org.strategy;

public class Client {

    public static void main(String[] args) {
        WildDuck wildDuck = new WildDuck();
        wildDuck.setFlyBehavior(new GoodFlyBehavior());
        wildDuck.setQuackBehavior(new NormaQuackBehavior());

        wildDuck.display();
        wildDuck.quack();
        wildDuck.fly();

        ToyDuck toyDuck = new ToyDuck();
        toyDuck.setFlyBehavior(new NoFlyBehavior());
        toyDuck.setQuackBehavior(new NoQuackBehavior());

        toyDuck.display();
        toyDuck.quack();
        toyDuck.fly();
    }

}

