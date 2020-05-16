package org.strategy;

public class NoQuackBehavior implements IQuackBehavior {
    @Override
    public void quack() {
        System.out.println("No quack.");
    }
}
