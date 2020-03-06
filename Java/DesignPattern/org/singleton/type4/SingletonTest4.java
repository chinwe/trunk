package org.singleton.type4;

public class SingletonTest4 {

	public static void main(String[] args) {
		Singleton instance1 = Singleton.INSTANCE;
		Singleton instance2 = Singleton.INSTANCE;
		System.out.println("instance1 hashCode = " + instance1.hashCode());
		System.out.println("instance2 hashCode = " + instance2.hashCode());
	}

}

// 枚举
enum Singleton {
	INSTANCE;
	public void method() {
		
	}
}