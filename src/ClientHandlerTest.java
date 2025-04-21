import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class ClientHandlerTest {
    private ClientHandler handler;
    private StringWriter sw;
    private PrintWriter pw;
    private Database1 db;

    @BeforeEach
    public void setup() throws Exception {
        // Clear static Database1 in ClientHandler
        Field dbField = ClientHandler.class.getDeclaredField("db");
        dbField.setAccessible(true);
        db = (Database1) dbField.get(null);
        // Clear internal lists
        for (String fieldName : new String[]{"users", "items", "messages"}) {
            Field f = Database1.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            ((List<?>) f.get(db)).clear();
        }

        handler = new ClientHandler((Socket) null);
        sw = new StringWriter();
        pw = new PrintWriter(sw, true);
        Field outField = ClientHandler.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(handler, pw);
    }

    private String invokeCommand(String cmd) throws Exception {
        Method m = ClientHandler.class.getDeclaredMethod("handleCommand", String.class);
        m.setAccessible(true);
        sw.getBuffer().setLength(0);
        m.invoke(handler, cmd);
        return sw.toString();
    }

    @Test
    public void testRegisterAndLogin() throws Exception {
        String resp = invokeCommand("register alice pass 50.0");
        assertTrue(resp.contains("User registered."));
        resp = invokeCommand("login alice pass");
        assertTrue(resp.contains("Login successful."));

        // Invalid login
        resp = invokeCommand("login alice wrong");
        assertTrue(resp.contains("Invalid credentials."));
    }

    @Test
    public void testAddAndSearchItem() throws Exception {
        invokeCommand("register bob pw 100.0");
        invokeCommand("login bob pw");
        String resp = invokeCommand("additem book 20.5");
        assertTrue(resp.contains("Item added to inventory"));

        resp = invokeCommand("searchitem book");
        assertFalse(resp.contains("Found item: book, $20.50, Seller: bob"));

        invokeCommand("sellitem book");
        resp = invokeCommand("searchitem book");

        assertTrue(resp.contains("Found item: book, $20.50, Seller: bob"));

        // Search nonexistent
        resp = invokeCommand("searchitem phone");
        assertTrue(resp.contains("Item not found or not for sale"));
    }

    @Test
    public void testBuyAndBalance() throws Exception {
        invokeCommand("register seller pw 100.0");
        invokeCommand("login seller pw");
        invokeCommand("additem pen 10.00");
        invokeCommand("sellitem pen");
        invokeCommand("logout");

        invokeCommand("register buyer pw 50.0");
        invokeCommand("login buyer pw");
        String resp = invokeCommand("buy pen");
        assertTrue(resp.contains("Transaction processed."));

        resp = invokeCommand("getbalance");
        assertTrue(resp.contains("$40.00"));
    }

    @Test
    public void testMessagingAndView() throws Exception {
        invokeCommand("register a pw 100.0");
        invokeCommand("register b pw 100.0");
        invokeCommand("login a pw");
        invokeCommand("sendmessage b Hello");
        String resp = invokeCommand("viewuserlist");
        assertTrue(resp.contains("b"));
        resp = invokeCommand("viewconversation b");
        assertTrue(resp.contains("a b Hello"));
    }

    @Test
    public void testDeleteOwnItem() throws Exception {
        invokeCommand("register u pw 100.0");
        invokeCommand("login u pw");
        invokeCommand("additem x 5.00");
        String resp = invokeCommand("deleteitem x");
        assertTrue(resp.contains("Item not found."));

        // Cannot delete again
        resp = invokeCommand("deleteitem x");
        assertTrue(resp.contains("Item not found."));
    }
}
