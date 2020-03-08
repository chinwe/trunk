package org.factory.factorymethod.pizzastore.pizza;

public class BJCheesePizza extends Pizza {

	public BJCheesePizza() {
		this.setName("BJCheesePizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare BJCheesePizza.");
	}

}
