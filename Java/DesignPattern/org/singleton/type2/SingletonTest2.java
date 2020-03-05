package org.singleton.type2;

public class SingletonTest2 {

	public static void main(String[] args) {
		Singleton instance1 = Singleton.getInstance();
		Singleton instance2 = Singleton.getInstance();
		System.out.println("instance1 hashCode = " + instance1.hashCode());
		System.out.println("instance2 hashCode = " + instance1.hashCode());
	}	
}

// 双重检测
class Singleton {	
	private static Singleton instance;

	private Singleton() {
		
	}
	
	public static Singleton getInstance() {
		
		if (null == instance) {
			synchronized (Singleton.class) {
				if (null == instance) {
					instance = new Singleton();
				}
			}
		}
		return instance;
	}
}