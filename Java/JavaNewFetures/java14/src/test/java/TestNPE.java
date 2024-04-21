import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * -XX:+ShowCodeDetailsInExceptionMessages
 * @author chinwe
 * 2024/4/21
 */
class TestNPE {

    @Test
    void testNPE() {
        Person person = new Person();
        assertThrows(NullPointerException.class, () -> {
            System.out.println(person.phone.length());
        });
    }

    private static class Person {
        String name;
        int age;
        String address;
        String phone;
    }
}
