package org.factory.factorymethod.pizza;

public abstract class Pizza {
	// name
	protected String name;
	
	public void setName(String name) {
		this.name = name;
	}

	public abstract void prepare();
	
	public void bake() {
		System.out.println(name + " baking;");
	}
	
	public void cut() {
		System.out.println(name + " cutting;");
	}
	
	public void box() {
		System.out.println(name + " boxing;");
	}
}
