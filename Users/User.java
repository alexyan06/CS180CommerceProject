import java.util.ArrayList;

public class User implements UserInterface{
    private String username;
    private String password;
    private double balance;
    private boolean type; //true = buyer, false = seller
    private ArrayList<String> messageUsername;
    private ArrayList<Item> ownedItems;

    public User(String username, String password, double balance, ArrayList<String> messageUsername) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.messageUsername = messageUsername;
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

    public ArrayList<String> getMessageUsername() {
        return messageUsername;
    }

    public void setMessageUsername(ArrayList<String> messageUsername) {
        this.messageUsername = messageUsername;
    }

    public void addMessageUsername(String messageUsername) {
        this.messageUsername.add(messageUsername);
    }

    public String toString() {
        return username;
    }
}
