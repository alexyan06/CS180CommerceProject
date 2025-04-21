import java.util.ArrayList;

/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public interface UserInterface {

    public ArrayList<Item> getOwnedItems();

    public void addOwnedItem(Item item);

    public void removeOwnedItem(Item item);

    public String getUsername();

    public String getPassword();

    public double getBalance();

    public void setBalance(double balance);

    public void setUsername(String username);

    public void setPassword(String password);

    public ArrayList<String> getMessageUsernameList();

    public void setMessageUsernameList(ArrayList<String> messageUsernameList);

    public void addMessageUsername(String messageUsername);

    public String toString();
}
