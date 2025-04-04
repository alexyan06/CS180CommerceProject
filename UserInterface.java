import java.util.ArrayList;

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

    public ArrayList<String> getMessageUsername();

    public void setMessageUsername(ArrayList<String> messageUsername);

    public void addMessageUsername(String messageUsername);

    public String toString();
}
