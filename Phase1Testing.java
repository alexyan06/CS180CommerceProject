import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * Phase 1 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, wang6377, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Phase1Testing {

    private Database1 db;

    @BeforeEach
    public void setup() {
        db = new Database1();
        db.addUser("alice", "pass1", 100.0, new ArrayList<>());
        db.addUser("bob", "pass2", 50.0, new ArrayList<>());
        db.addUser("charlie", "pass3", 75.0, new ArrayList<>());
    }

    @Test
    public void testLogin() {
        assertTrue(db.login("alice", "pass1"));
        assertFalse(db.login("alice", "wrong"));
        assertFalse(db.login("unknown", "pass"));
    }

    @Test
    public void testSearchItem() {
        db.addItem("Basketball", 30.0, "bob");
        Item found = db.searchItem("basketball");
        assertNotNull(found);
        assertEquals("Basketball", found.getName());

        assertNull(db.searchItem("Laptop"));
    }

    @Test
    public void testMessaging() {
        db.sendMessage("alice", "bob", "Hey Bob!");
        db.sendMessage("bob", "alice", "Hey Alice!");

        ArrayList<Message> convo = db.getSenderToReceiverMessage("alice", "bob");
        assertEquals(2, convo.size());

        ArrayList<Message> aliceMsgs = db.getSingleUserMessage("alice");
        assertEquals(2, aliceMsgs.size());
    }

    @Test
    public void testAddAndDeleteItem() {
        db.addItem("Headphones", 45.0, "bob");
        assertEquals(1, db.getItems().size());

        db.deleteItem("Headphones");
        assertEquals(0, db.getItems().size());

        User bob = db.getUser("bob");
        assertEquals(0, bob.getOwnedItems().size());
    }

    @Test
    public void testProcessTransaction() {
        db.addItem("Basketball", 30.0, "bob");

        User alice = db.getUser("alice");
        User bob = db.getUser("bob");
        Item basketball = db.searchItem("Basketball");

        db.processTransaction(alice, bob, basketball);

        assertEquals(70.0, alice.getBalance());
        assertEquals(80.0, bob.getBalance());
        assertEquals(1, alice.getOwnedItems().size());
        assertEquals(0, bob.getOwnedItems().size());
        assertEquals(0, db.getItems().size());
    }

    @Test
    public void testDeleteUserRemovesData() {
        db.addItem("Book", 20.0, "charlie");
        db.sendMessage("alice", "charlie", "Hey!");
        db.sendMessage("charlie", "alice", "Hello!");

        assertTrue(db.deleteUser("charlie", "pass3"));
        assertNull(db.getUser("charlie"));

        // Item and messages removed
        assertEquals(0, db.getItems().size());
        for (Message m : db.getMessages()) {
            assertFalse(m.getSender().equals("charlie") || m.getReceiver().equals("charlie"));
        }
    }
}
