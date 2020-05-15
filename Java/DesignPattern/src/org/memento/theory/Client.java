package org.memento.theory;

public class Client {

    public static void main(String[] args) {
        Originator originator = new Originator();
        Caretaker caretaker = new Caretaker();

        originator.setState("HP 100");
        caretaker.add(originator.saveState());

        originator.setState("HP 80");
        caretaker.add(originator.saveState());

        originator.setState("HP 50");
        caretaker.add(originator.saveState());

        System.out.println("Current State: " + originator.getState());

        originator.getStateMemento(caretaker.get(1));
        System.out.println("New State: " + originator.getState());
    }
}
