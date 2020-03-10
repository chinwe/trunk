package org.builder;

public abstract class HouseBuilder {
	
	protected House house = new House();
	
	public abstract void buildBaisc();
	
	public abstract void buildWalls();
	
	public abstract void roofed();
	
	public House buildHouse() {
		return house;
	}
}
