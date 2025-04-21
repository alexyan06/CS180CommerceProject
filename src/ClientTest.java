import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
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