package org.factory.simplefactory.pizzastore.order;

import org.factory.simplefactory.pizzastore.pizza.CheesePizza;
import org.factory.simplefactory.pizzastore.pizza.GreekPizza;
import org.factory.simplefactory.pizzastore.pizza.PepperPizza;
import org.factory.simplefactory.pizzastore.pizza.Pizza;

public class SimpleFactory {

	public Pizza createPizza(String orderType) {
		Pizza pizza = null;
		
		if (orderType.equals("greek")) {
			pizza = new GreekPizza();
		} else if (orderType.equals("cheese")) {
			pizza = new CheesePizza();
		} else if (orderType.equals("pepper")) {
			pizza = new PepperPizza();
		}
		
		return pizza;
	}
}
