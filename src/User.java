import java.util.ArrayList;

/**
 * Phase 1 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, wang6377, KayshavBhardwaj
 * @version April 6, 2025
 */
public class User implements UserInterface {
    private String username;
    private String password;
    private double balance;
    private ArrayList<String> messageUsernameList;
    private ArrayList<Item> ownedItems;

    public User(String username, String password, double balance, ArrayList<String> messageUsernameList) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.messageUsernameList = messageUsernameList;
        this.ownedItems = new ArrayList<>();
    }

    public ArrayList<Item> getOwnedItems() {
        return ownedItems;
    }

    public void addOwnedItem(Item item) {
        ownedItems.add(item);
    }

    public void removeOwnedItem(Item item) {
        ownedItems.remove(item);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getMessageUsernameList() {
        return messageUsernameList;
    }

    public void setMessageUsernameList(ArrayList<String> messageUsernameList) {
        this.messageUsernameList = messageUsernameList;
    }

    public void addMessageUsername(String messageUsername) {
        this.messageUsernameList.add(messageUsername);
    }

    public String toString() {
        return username;
    }
}
