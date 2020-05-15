package org.decorator;

public class Milk extends Decorator {

	public Milk(Drink drink) {
		super(drink);
		setDesc("Milk");
		setPrice(1.5f);
	}

}
