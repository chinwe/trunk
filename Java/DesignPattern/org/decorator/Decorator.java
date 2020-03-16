package org.decorator;

public class Decorator extends Drink {

	private Drink drink;
	
	public Decorator(Drink drink) {
		this.drink = drink;
	}

	@Override
	public float cost() {
		return drink.cost() + super.getPrice();
	}
	
	@Override
	public String getDesc() {
		return drink.getDesc() + " + " + super.getDesc();
	}
}
