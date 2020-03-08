package org.factory.factorymethod.order;

import org.factory.factorymethod.pizza.BJCheesePizza;
import org.factory.factorymethod.pizza.BJPepperPizza;
import org.factory.factorymethod.pizza.Pizza;

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
