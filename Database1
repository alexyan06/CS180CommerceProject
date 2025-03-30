import java.util.ArrayList;

public class Database1 {
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<Message> messages = new ArrayList<Message>();

    public synchronized boolean addUser(String username, String password, double balance, boolean type) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }
        users.add(new User(username, password, balance, type));

        return true;
    }

    public synchronized boolean deleteUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                users.remove(user);
                return true;
            }
        }
        return false;
    }


    public synchronized User getUser(String username) {
        for (User user: users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public synchronized boolean addItem(String name, double cost, String seller) {
        for (Item item : items) {
            if (item.getName().equals(name)) {
                return false;
            }
        }

        items.add(new Item(name, cost, seller));
        return true;
    }

    public synchronized boolean deleteItem(String name) {
        for (Item item : items) {
            if (item.getName().equals(name)) {
                items.remove(item);
                return true;
            }
        }
        return false;
    }

    public synchronized ArrayList<Item> getItems() {
        return new ArrayList<>(items);
    }

    public synchronized void sendMessage(String sender, String receiver, String message) {
        messages.add(new Message(sender, receiver, message));
    }

    public synchronized ArrayList<Message> getMessages() {
        return new ArrayList<>(messages);
    }
}
