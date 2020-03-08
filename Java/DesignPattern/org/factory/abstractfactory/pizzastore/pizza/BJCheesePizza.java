package org.factory.abstractfactory.pizzastore.pizza;

public class BJCheesePizza extends Pizza {

	public BJCheesePizza() {
		this.setName("BJCheesePizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare BJCheesePizza.");
	}

}
