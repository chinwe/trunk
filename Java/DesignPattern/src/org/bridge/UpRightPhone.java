package org.bridge;

public class UpRightPhone extends Phone {

	public UpRightPhone(Brand brand) {
		super(brand);
	}
	
	public void open() {
		super.open();
		System.out.println("UpRight phone.");
	}
	
	public void close() {
		super.close();
		System.out.println("UpRight phone.");
	}
	
	public void call() {
		super.call();
		System.out.println("UpRight phone.");
	}
}
