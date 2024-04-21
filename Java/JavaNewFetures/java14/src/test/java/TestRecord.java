import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author chinwe
 * 2024/4/21
 */
class TestRecord {

    @Test
    void testRecord() {
        // Create a new Person object
        Person person = new Person("chinwe", 18);

        // Assert that the name of the person is "chinwe"
        assertEquals("chinwe", person.name());

        // Assert that the age of the person is 18
        assertEquals(18, person.age());
    }

    private record Person(String name, int age) {
    }
}
