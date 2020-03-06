package org.factory.simplefactory.pizzastore.order;

import java.util.Scanner;

import org.factory.simplefactory.pizzastore.pizza.Pizza;

public class OrderPizza {
	
	public void setSimpleFactory(SimpleFactory simpleFactory) {
		
		String orderType;
		do {
			orderType = getType();
			
			Pizza pizza = simpleFactory.createPizza(orderType);
			
			if (pizza != null) {
				pizza.prepare();
				pizza.bake();
				pizza.cut();
				pizza.box();
				
				System.out.println("Pizza order.");
			} else {
				System.out.println("Unknown pizza type.");
			}
		} while (!orderType.equals("q"));
	}

	private String getType() {
		System.out.print("Input pizza type(greek cheese pepper) or q(Quit):");
		Scanner in = new Scanner(System.in);
		return in.next();
	}
}
