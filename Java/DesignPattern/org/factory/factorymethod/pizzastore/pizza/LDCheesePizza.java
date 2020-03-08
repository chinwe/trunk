package org.factory.factorymethod.pizzastore.pizza;

public class LDCheesePizza extends Pizza {

	public LDCheesePizza() {
		this.setName("LDCheesePizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare LDCheesePizza.");
	}

}
