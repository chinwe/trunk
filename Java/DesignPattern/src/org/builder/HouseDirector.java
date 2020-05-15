package org.builder;

public class HouseDirector {

	private HouseBuilder houseBuilder = null;
	
	public void setHouseBuilder(HouseBuilder houseBuilder) {
		this.houseBuilder = houseBuilder;
	}
	
	public House buildHouse() {
		houseBuilder.buildBaisc();
		houseBuilder.buildWalls();
		houseBuilder.roofed();
		
		return houseBuilder.buildHouse();
	}
}
