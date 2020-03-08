package org.factory.abstractfactory.pizzastore.order;

public class PizzaStore {

	public static void main(String[] args) {

		OrderPizza orderPizza = new OrderPizza();
		orderPizza.setFactory(new LDFactory());
	}
}
