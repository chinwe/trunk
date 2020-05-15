package org.builder;

public class HighBuildingBuilder extends HouseBuilder {

	@Override
	public void buildBaisc() {
		System.out.println("High building build basic.");
	}

	@Override
	public void buildWalls() {
		System.out.println("High building build walls.");
	}

	@Override
	public void roofed() {
		System.out.println("High building roofed.");
	}


}
