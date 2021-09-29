package chapter10;

/**
 * 第74条：每个方法抛出的所有异常都要建立文档
 *
 */
public class Item74 {

    /**
     *
     * @throws UnsupportedOperationException 不支持
     */
    public static void foo() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {

    }
}
