import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author chinwe
 * 2024/1/30
 */
class TestTextBlock {
    @Test
    void testTextBlock() {
        String htmlBefore ="<html>\n" +
                "    <body>\n" +
                "        <p>Hello, World</p>\n" +
                "    </body>\n" +
                "</html>\n";

        String html = """
                <html>
                    <body>
                        <p>Hello, World</p>
                    </body>
                </html>
                """;
        assertEquals(html, htmlBefore);
    }
}
