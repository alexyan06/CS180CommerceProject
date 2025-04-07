import java.util.ArrayList;
/**
 * Phase 1 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, wang6377, KayshavBhardwaj
 * @version April 6, 2025
 */
public interface DatabaseInterface {
    boolean addUser(String username, String password, double balance, ArrayList<String> messageUser);
    boolean login(String username, String password);
    boolean deleteUser(String username, String password);
    boolean addItem(String name, double cost, String sellerUsername);
    boolean deleteItem(String name);
    Item searchItem(String name);
    void sendMessage(String sender, String receiver, String message);
    ArrayList<Message> getMessages();
    ArrayList<Message> getSingleUserMessage(String username);
    ArrayList<Message> getSenderToReceiverMessage(String sender, String receiver);
    void processTransaction(User buyer, User seller, Item item);
}
