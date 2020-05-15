package org.factory.abstractfactory.pizzastore.order;

import org.factory.abstractfactory.pizzastore.pizza.Pizza;
import org.factory.abstractfactory.pizzastore.pizza.BJCheesePizza;
import org.factory.abstractfactory.pizzastore.pizza.BJPepperPizza;

public class BJFactory extends AbstractFactory {

	@Override
	Pizza createPizza(String orderType) {
		Pizza pizza = null;
		
		if (orderType.equals("cheese")) {
			pizza = new BJCheesePizza();
		} else if (orderType.equals("pepper")) {
			pizza = new BJPepperPizza();
		}
		
		return pizza;
	}

}
