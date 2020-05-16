package org.strategy;

public abstract class BaseDuck {

    IFlyBehavior flyBehavior;
    IQuackBehavior quackBehavior;

    public void setFlyBehavior(IFlyBehavior flyBehavior) {
        this.flyBehavior = flyBehavior;
    }

    public void setQuackBehavior(IQuackBehavior quackBehavior) {
        this.quackBehavior = quackBehavior;
    }

    public abstract void display();

    public void quack() {
        if (quackBehavior != null) {
            quackBehavior.quack();
        }
    }

    public void fly() {
        if (flyBehavior != null) {
            flyBehavior.fly();
        }
    }

}
