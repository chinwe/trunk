package org.builder;

public class CommonHouseBuilder extends HouseBuilder {

	@Override
	public void buildBaisc() {
		System.out.println("Common house build basic.");
	}

	@Override
	public void buildWalls() {
		System.out.println("Common house build walls.");
	}

	@Override
	public void roofed() {
		System.out.println("Common house roofed.");
	}

}
