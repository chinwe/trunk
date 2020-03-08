package org.factory.abstractfactory.pizzastore.order;

import java.util.Scanner;

import org.factory.abstractfactory.pizzastore.pizza.Pizza;

public class OrderPizza {
	
	AbstractFactory factory;
	
	public void setFactory(AbstractFactory factory) {
		this.factory = factory;
		String orderType;
		do {
			orderType = getType();
			
			if (orderType.equals("q")) {
				break;
			}
			
			Pizza pizza = this.factory.createPizza(orderType);
			
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
