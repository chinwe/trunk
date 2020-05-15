package org.factory.factorymethod.pizzastore.order;

import java.util.Scanner;

import org.factory.factorymethod.pizzastore.pizza.Pizza;


public abstract class OrderPizza {
	
	abstract Pizza creatPizza(String orderType);

	public OrderPizza() {
		
		String orderType;
		do {
			orderType = getType();
			
			if (orderType.equals("q")) {
				break;
			}
			
			Pizza pizza = creatPizza(orderType);
			
			if (pizza != null) {
				pizza.prepare();
				pizza.bake();
				pizza.cut();
				pizza.box();
				
				System.out.println("Pizza order.");
			} else {
				System.out.println("Unknown pizza type.");
			}
		} while (true);
	}

	private String getType() {
		System.out.print("Input pizza type(cheese pepper) or q(Quit):");
		Scanner in = new Scanner(System.in);
		return in.next();
	}
}
