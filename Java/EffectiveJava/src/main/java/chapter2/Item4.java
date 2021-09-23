package chapter2;

/**
 * 第4条：通过私有构造器强化不可实例化的能力
 *
 */
public class Item4 {

    public static class UlilityClass {
        // Suppresses default constructor, ensuring non-instantiability.
        private UlilityClass() {
            throw new AssertionError();
        }
    }

    public static void main(String[] args) {

        UlilityClass ulilityClass = new UlilityClass();
    }
}
