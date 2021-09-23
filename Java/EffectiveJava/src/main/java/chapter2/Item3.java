package chapter2;

/**
 * 第3条：用私有构造器或者枚举类型强化Singleton属性
 *
 * 单元素的枚举类型经常成为实现Singleton的最佳方法
 *
 */
public class Item3 {

    // 单例 静态工厂方法
    public static class Elvis {
        private static final Elvis instance = new Elvis();

        private Elvis() {
        }

        public static Elvis getInstance() {
            return instance;
        }

        public void doSomeThing() {
            System.out.println("doSomeThing");
        }
    }

    // 单例 枚举实现
    public enum ElvisEnum {
        INSTANCE;

        public void doSomeThing() {
            System.out.println("doSomeThing");
        }
    }

    public static void main(String[] args) {

        Elvis.getInstance().doSomeThing();

        ElvisEnum.INSTANCE.doSomeThing();
    }
}
