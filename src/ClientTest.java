import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {
    @Test
    public void testConstants() {
        assertEquals("localhost", Client.HOST);
        assertEquals(Server.PORT, Client.PORT);
    }

    @Test
    public void testMainMethodExists() throws NoSuchMethodException {
        assertNotNull(Client.class.getMethod("main", String[].class));
    }
}