package org.factory.abstractfactory.pizzastore.pizza;

public class LDPepperPizza extends Pizza {

	public LDPepperPizza() {
		this.setName("LDPepperPizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare LDPepperPizza.");
	}

}
