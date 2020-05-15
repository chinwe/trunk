package org.principle.liskov;

class A {
	public int foo(int a, int b) {
		return a - b;
	}
}

class B extends A {
	// 不建议重写
	public int foo(int a, int b) {
		return a + b;
	}
	
	public int bar(int a, int b) {
		return foo(a, b) + 9;
	}
}

public class LiskovSubstitution {
	
	public static void main(String[] args) {
		B b = new B();
		System.out.println(b.bar(1, 2));
	}
}
