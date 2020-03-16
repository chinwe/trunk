package org.decorator;

public class Coffee extends Drink {

	@Override
	public float cost() {
		return getPrice();
	}

}
