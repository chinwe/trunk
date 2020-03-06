package org.factory.simplefactory.pizzastore.pizza;

public class PepperPizza extends Pizza {

	public PepperPizza() {
		this.setName("PepperPizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare PepperPizza.");
	}

}
