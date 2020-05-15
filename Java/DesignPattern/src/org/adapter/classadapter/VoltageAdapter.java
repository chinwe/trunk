package org.adapter.classadapter;

public class VoltageAdapter extends Voltage200V implements IVoltage5V {

	@Override
	public int output5V() {
		int src = super.output200V();
		int dst = src / 44;
		System.out.println("Adapter " + src + "V to " + dst + "V");
		System.out.println("output " + dst + "V");
		return dst;
	}

}
