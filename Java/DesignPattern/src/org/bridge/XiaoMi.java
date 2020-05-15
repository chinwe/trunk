/**
 * 
 */
package org.bridge;

public class XiaoMi implements Brand {

	@Override
	public void open() {
		System.out.println("XiaoMi phone open.");
	}

	@Override
	public void close() {
		System.out.println("XiaoMi phone close.");

	}

	@Override
	public void call() {
		System.out.println("XiaoMi phone call.");

	}

}
