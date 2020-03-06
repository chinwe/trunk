package org.factory.simplefactory.pizzastore.pizza;

public class CheesePizza extends Pizza {

	public CheesePizza() {
		this.setName("CheesePizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare CheesePizza.");
	}

}
