package org.decorator;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Drink drink = new Espresso();
		System.out.println(drink.getDesc());
		System.out.println(drink.cost());
		
		drink = new Milk(drink);
		System.out.println(drink.getDesc());
		System.out.println(drink.cost());
		
		drink = new Chocolate(drink);
		System.out.println(drink.getDesc());
		System.out.println(drink.cost());
	}
}
