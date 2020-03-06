package org.factory.simplefactory.pizzastore.pizza;

public class GreekPizza extends Pizza {

	public GreekPizza() {
		this.setName("GreekPizza");
	}
	
	@Override
	public void prepare() {
		System.out.println("Prepare GreekPizza.");
	}

}
