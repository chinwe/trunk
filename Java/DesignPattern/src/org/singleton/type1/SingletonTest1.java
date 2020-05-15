package org.singleton.type1;

public class SingletonTest1 {

	public static void main(String[] args) {
		Singleton instance1 = Singleton.getInstance();
		Singleton instance2 = Singleton.getInstance();
		System.out.println("instance1 hashCode = " + instance1.hashCode());
		System.out.println("instance2 hashCode = " + instance2.hashCode());
	}	
}

//静态初始化
class Singleton {	
	private Singleton() {
		
	}
	
	private static Singleton instance = new Singleton();

	public static Singleton getInstance() {
		return instance;
	}
}