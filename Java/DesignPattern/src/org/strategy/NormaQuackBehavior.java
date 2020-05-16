package org.strategy;

public class NormaQuackBehavior implements IQuackBehavior {
    @Override
    public void quack() {
        System.out.println("Quack.");
    }
}
