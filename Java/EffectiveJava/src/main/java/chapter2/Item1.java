package chapter2;

/**
 * 第1条：用静态工厂方法代替构造器
 * static factory method
 *
 * 与构造器不同的优势：
 * 1、有名称，可以根据不同参数定义不同名称，方便理解
 * 2、不必每次调用它们的时候都创建一个新对象（享元模式）
 * 3、可以返回原返回类型的任何子类型的对象
 * 4、所返回的对象的类剋随着每次调用而发生变化，这取决于静态工厂方法的参数值
 * 5、方法返回的对象所属的类，在编写包含该静态工厂方法的类时可以不存在
 * 主要缺点：
 * 1、类如果不含公有的或者受保护的构造器，就不能被子类化
 * 2、程序员很难发现他们
 */
public class Item1 {

    public static void main(String[] args) {
        System.out.println(Boolean.valueOf(false));
    }
}
