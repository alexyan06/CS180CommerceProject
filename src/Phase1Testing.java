import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        // Create a new User before each test
        user = new User("alice", "pwd", 100.0, new ArrayList<>());
    }

    @Test
    void testConstructorAndGetters() {
        // Verify that the constructor correctly sets username, password, balance,
        // and initializes owned items and message lists as empty
        assertEquals("alice", user.getUsername());
        assertEquals("pwd", user.getPassword());
        assertEquals(100.0, user.getBalance());
        assertTrue(user.getOwnedItems().isEmpty());
        assertTrue(user.getMessageUsernameList().isEmpty());
    }

    @Test
    void testUsernamePasswordSetters() {
        // Verify that setUsername and setPassword properly update the fields
        user.setUsername("bob");
        user.setPassword("pass");
        assertEquals("bob", user.getUsername());
        assertEquals("pass", user.getPassword());
    }

    @Test
    void testBalanceSetter() {
        // Verify that setBalance correctly updates the user's balance
        user.setBalance(50.0);
        assertEquals(50.0, user.getBalance());
    }

    @Test
    void testOwnedItemsManipulation() {
        // Verify adding and removing items from the owned items list
        Item item = new Item("Book", 20.0, "seller");
        user.addOwnedItem(item);
        assertTrue(user.getOwnedItems().contains(item));
        user.removeOwnedItem(item);
        assertFalse(user.getOwnedItems().contains(item));
    }

    @Test
    void testMessageUsernameList() {
        // Verify adding a single contact and resetting the entire message username list
        user.addMessageUsername("bob");
        assertTrue(user.getMessageUsernameList().contains("bob"));
        ArrayList<String> list = new ArrayList<>(Arrays.asList("x", "y"));
        user.setMessageUsernameList(list);
        assertEquals(list, user.getMessageUsernameList());
    }

    @Test
    void testToString() {
        // Verify that toString returns the username
        assertEquals("alice", user.toString());
    }
}

class MessageTest {
    private Message msg;

    @BeforeEach
    void setUp() {
        // Create a new Message before each test
        msg = new Message("alice", "bob", "hello");
    }

    @Test
    void testConstructorAndGetters() {
        // Verify constructor sets sender, receiver, and message correctly
        assertEquals("alice", msg.getSender());
        assertEquals("bob", msg.getReceiver());
        assertEquals("hello", msg.getMessage());
    }

    @Test
    void testToString() {
        // Verify that toString concatenates sender, receiver, and message
        assertEquals("alice bob hello", msg.toString());
    }
}

class ItemTest {
    private Item item;

    @BeforeEach
    void setUp() {
        // Create a new Item before each test
        item = new Item("Laptop", 999.99, "seller");
    }

    @Test
    void testConstructorAndGetters() {
        // Verify constructor sets name, cost, and seller correctly
        assertEquals("Laptop", item.getName());
        assertEquals(999.99, item.getCost());
        assertEquals("seller", item.getSeller());
    }

    @Test
    void testSetters() {
        // Verify setters update name, cost, and seller correctly
        item.setName("Phone");
        item.setCost(199.99);
        item.setSeller("alice");
        assertEquals("Phone", item.getName());
        assertEquals(199.99, item.getCost());
        assertEquals("alice", item.getSeller());
    }

    @Test
    void testToString() {
        // Verify that toString returns the item name
        assertEquals("Laptop", item.toString());
    }
}

class Database1Test {
    private Database1 db;

    @BeforeEach
    void setup() {
        // Initialize the database and add two users before each test
        db = new Database1();
        db.addUser("alice","pwd1",100.0,new ArrayList<>());
        db.addUser("bob","pwd2",50.0,new ArrayList<>());
    }

    @Test
    void testAddUserDuplicate() {
        // Ensure adding a user with an existing username fails
        assertFalse(db.addUser("alice","x",0,new ArrayList<>()));
    }

    @Test
    void testDeleteUserWrongCredentials() {
        // Ensure deletion fails with incorrect password and the user remains
        assertFalse(db.deleteUser("alice","wrong"));
        assertNotNull(db.getUser("alice"));
    }

    @Test
    void testDeleteUserClearsData() {
        // Ensure deleting a user removes their items and messages
        db.addItem("Book",20.0,"alice");
        db.sendMessage("alice","bob","msg");
        assertTrue(db.deleteUser("alice","pwd1"));
        assertNull(db.getUser("alice"));
        assertTrue(db.getItems().isEmpty());
        assertTrue(db.getMessages().stream()
                .noneMatch(m->m.getSender().equals("alice")||m.getReceiver().equals("alice")));
    }

    @Test
    void testAddAndSearchItem() {
        // Verify adding a new item works and it can be searched case-insensitively
        assertTrue(db.addItem("Ball",10.0,"bob"));
        Item found = db.searchItem("ball");
        assertNotNull(found);
        assertEquals("Ball",found.getName());
        // Ensure duplicate item names are rejected
        assertFalse(db.addItem("Ball",10.0,"bob"));
    }

    @Test
    void testLogin() {
        // Verify login succeeds with correct credentials and fails otherwise
        assertTrue(db.login("alice","pwd1"));
        assertFalse(db.login("alice","wrong"));
    }

    @Test
    void testMessagingLists() {
        // Verify sending a message adds it to both sender-receiver lists
        db.sendMessage("alice","bob","hi");
        List<Message> conv = db.getSenderToReceiverMessage("alice","bob");
        assertEquals(1, conv.size());
        List<Message> aliceMsgs = db.getSingleUserMessage("alice");
        assertEquals(1, aliceMsgs.size());
    }

    @Test
    void testProcessTransactionInsufficientFunds() {
        // Verify that a transaction with insufficient funds is not processed
        db.addItem("Expensive",200.0,"bob");
        User buyer=db.getUser("alice");
        User seller=db.getUser("bob");
        Item it=db.searchItem("Expensive");
        db.processTransaction(buyer,seller,it);
        assertEquals(100.0,buyer.getBalance());
        assertEquals(50.0,seller.getBalance());
        assertNotNull(db.searchItem("Expensive"));
    }

    @Test
    void testProcessTransactionSuccess() {
        // Verify a valid transaction updates balances and ownership, and removes item
        db.addItem("Cheap",20.0,"bob");
        User buyer=db.getUser("alice");
        User seller=db.getUser("bob");
        Item it=db.searchItem("Cheap");
        db.processTransaction(buyer,seller,it);
        assertEquals(80.0,buyer.getBalance());
        assertEquals(70.0,seller.getBalance());
        assertTrue(buyer.getOwnedItems().contains(it));
        assertFalse(db.getItems().contains(it));
    }

    @Test
    void testDisplayThreadFormatting() {
        // Verify displayThread returns a properly formatted conversation string
        db.sendMessage("alice","bob","hello");
        db.sendMessage("bob","alice","hi");
        String thread = db.displayThread("alice","bob");
        assertTrue(thread.contains("alice bob hello\n"));
        assertTrue(thread.contains("bob alice hi\n"));
    }
}

class ClientHandlerTest {
    private ClientHandler handler;
    private ByteArrayOutputStream baos;

    @BeforeEach
    void setup() throws Exception {
        // Prepare a ClientHandler and redirect its output to a byte stream
        handler = new ClientHandler((Socket) null);
        baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos, true);
        Field outField = ClientHandler.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(handler, pw);
    }

    private String exec(String cmd) throws Exception {
        // Invoke handleCommand using reflection and capture the output
        Method handle = ClientHandler.class.getDeclaredMethod("handleCommand", String.class);
        handle.setAccessible(true);
        baos.reset();
        handle.invoke(handler, cmd);
        return baos.toString().trim();
    }

    @Test
    void testRegisterLoginAndLogout() throws Exception {
        // Verify register, login, and logout commands produce expected responses
        String r = exec("register alice pwd 100");
        assertEquals("User registered.", r);
        String l = exec("login alice pwd");
        assertEquals("Login successful.", l);
        String out = exec("logout");
        assertEquals("Logged out.", out);
    }

    @Test
    void testAddItemWithoutLogin() throws Exception {
        // Ensure additem command fails when no user is logged in
        String resp = exec("additem Item 10");
        assertEquals("Please login first.", resp);
    }

    @Test
    void testUnknownCommand() throws Exception {
        // Verify an unrecognized command returns the Unknown command message
        String resp = exec("foo");
        assertTrue(resp.startsWith("Unknown command:"));
    }
}

class ServerTest {
    @Test
    void testPortConstant() {
        // Verify Server.PORT is set correctly
        assertEquals(12345, Server.PORT);
    }
}

class ClientTest {
    @Test
    void testDefaults() {
        // Verify Client.HOST and Client.PORT defaults
        assertEquals("localhost", Client.HOST);
        assertEquals(12345, Client.PORT);
    }
}

class Client2Test {
    @Test
    void testDefaults() {
        // Verify Client2.HOST and Client2.PORT defaults
        assertEquals("localhost", Client2.HOST);
        assertEquals(12345, Client2.PORT);
    }
}
