package org.builder;

public class Client {

	public static void main(String[] args) {

		HouseBuilder houseBuilder = new HighBuildingBuilder();
		
		HouseDirector houseDirector = new HouseDirector();
		houseDirector.setHouseBuilder(houseBuilder);
		
		houseDirector.buildHouse();
	}

}
