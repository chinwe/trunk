package org.factory.factorymethod.order;

import org.factory.factorymethod.pizza.LDCheesePizza;
import org.factory.factorymethod.pizza.LDPepperPizza;
import org.factory.factorymethod.pizza.Pizza;

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
