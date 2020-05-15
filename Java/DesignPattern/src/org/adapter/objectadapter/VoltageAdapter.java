package org.adapter.objectadapter;

public class VoltageAdapter implements IVoltage5V {

	private Voltage200V voltage200V = null;
	
	public VoltageAdapter(Voltage200V voltage200v) {
		super();
		voltage200V = voltage200v;
	}
	
	@Override
	public int output5V() {
		int src = voltage200V.output200V();
		int dst = src / 44;
		System.out.println("Adapter " + src + "V to " + dst + "V");
		System.out.println("output " + dst + "V");
		return dst;
	}

}
