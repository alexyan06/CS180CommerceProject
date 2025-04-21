import org.junit.jupiter.api.BeforeAll;
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
public class ServerTest {
    private static Thread serverThread;
    private static Server server;

    @BeforeAll
    public static void setup() {
        server = new Server();
        serverThread = new Thread(() -> {
            try {
                server.main(new String[]{});
            } catch (Exception ignored) {

            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Test
    public void testPortConstant() {
        assertEquals(12345, Server.PORT);
    }

    @Test
    public void testMainMethodExists() throws NoSuchMethodException {
        // Ensure main method signature is correct
        assertNotNull(Server.class.getMethod("main", String[].class));
    }
}