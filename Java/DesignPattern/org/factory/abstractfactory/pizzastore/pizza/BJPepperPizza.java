package org.factory.abstractfactory.pizzastore.pizza;

public class BJPepperPizza extends Pizza {

	public BJPepperPizza() {
		this.setName("BJPepperPizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare BJPepperPizza.");
	}

}
