package org.adapter.objectadapter;

public class Phone {

	public void charging(IVoltage5V voltage5v) {
		int voltage = voltage5v.output5V();
		if (5 == voltage) {
			System.out.println("Charging...");
		} else {
			System.out.println("Voltage mismatch.");
		}
	}
}
