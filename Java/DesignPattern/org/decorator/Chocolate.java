package org.decorator;

public class Chocolate extends Decorator {

	public Chocolate(Drink drink) {
		super(drink);
		setDesc("Chocolate");
		setPrice(1.0f);
	}

}
