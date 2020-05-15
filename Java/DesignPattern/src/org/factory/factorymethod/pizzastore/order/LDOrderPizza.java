package org.factory.factorymethod.pizzastore.order;

import org.factory.factorymethod.pizzastore.pizza.LDCheesePizza;
import org.factory.factorymethod.pizzastore.pizza.LDPepperPizza;
import org.factory.factorymethod.pizzastore.pizza.Pizza;

public class LDOrderPizza extends OrderPizza {

	@Override
	Pizza creatPizza(String orderType) {
		Pizza pizza = null;
		
		if (orderType.equals("cheese")) {
			pizza = new LDCheesePizza();
		} else if (orderType.equals("pepper")) {
			pizza = new LDPepperPizza();
		}
		
		return pizza;
	}

}
