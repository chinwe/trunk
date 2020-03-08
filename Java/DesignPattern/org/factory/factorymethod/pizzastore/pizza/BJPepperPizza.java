package org.factory.factorymethod.pizzastore.pizza;

public class BJPepperPizza extends Pizza {

	public BJPepperPizza() {
		this.setName("BJPepperPizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare BJPepperPizza.");
	}

}
