package org.adapter.objectadapter;

public class Client {

	public static void main(String[] args) {
		
		Phone phone = new Phone();
		
		VoltageAdapter voltageAdapter = new VoltageAdapter(new Voltage200V());
		phone.charging(voltageAdapter);
	}

}
