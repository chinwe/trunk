package org.factory.simplefactory.pizzastore.order;

public class PizzaStore {

	public static void main(String[] args) {
		OrderPizza orderPizza = new OrderPizza();
		orderPizza.setSimpleFactory(new SimpleFactory());
	}

}
