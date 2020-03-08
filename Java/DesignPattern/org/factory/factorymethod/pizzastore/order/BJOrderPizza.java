package org.factory.factorymethod.pizzastore.order;

import org.factory.factorymethod.pizzastore.pizza.BJCheesePizza;
import org.factory.factorymethod.pizzastore.pizza.BJPepperPizza;
import org.factory.factorymethod.pizzastore.pizza.Pizza;

public class BJOrderPizza extends OrderPizza {

	@Override
	Pizza creatPizza(String orderType) {
		Pizza pizza = null;
		
		if (orderType.equals("cheese")) {
			pizza = new BJCheesePizza();
		} else if (orderType.equals("pepper")) {
			pizza = new BJPepperPizza();
		}
		
		return pizza;
	}

}
