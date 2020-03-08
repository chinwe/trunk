package org.factory.abstractfactory.pizzastore.order;

import org.factory.abstractfactory.pizzastore.pizza.Pizza;

abstract class AbstractFactory {
	abstract Pizza createPizza(String orderType);
}
