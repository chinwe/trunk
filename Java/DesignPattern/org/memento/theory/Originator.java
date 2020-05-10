package org.memento.theory;

public class Originator {

    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Memento saveState() {
        return new Memento(state);
    }

    public void getStateMemento(Memento memento) {
        state = memento.getState();
    }
}
