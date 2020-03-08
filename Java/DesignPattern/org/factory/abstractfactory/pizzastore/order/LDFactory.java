package org.factory.abstractfactory.pizzastore.order;

import org.factory.abstractfactory.pizzastore.pizza.Pizza;
import org.factory.abstractfactory.pizzastore.pizza.LDCheesePizza;
import org.factory.abstractfactory.pizzastore.pizza.LDPepperPizza;

public class LDFactory extends AbstractFactory {

	@Override
	Pizza createPizza(String orderType) {
		Pizza pizza = null;
		
		if (orderType.equals("cheese")) {
			pizza = new LDCheesePizza();
		} else if (orderType.equals("pepper")) {
			pizza = new LDPepperPizza();
		}
		
		return pizza;
	}

}
