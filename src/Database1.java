import java.util.ArrayList;
import java.util.Iterator;

/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Database1 implements DatabaseInterface {
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<Item> ownedItems = new ArrayList<>();

    @Override
    public synchronized boolean addUser(String username, String password,
                                        double balance, ArrayList<String> messageList) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return false;
            }
        }
        users.add(new User(username, password, balance, messageList));
        return true;
    }

    @Override
    public synchronized boolean deleteUser(String username, String password) {
        User toRemove = getUser(username);
        if (toRemove != null && toRemove.getPassword().equals(password)) {
            items.removeIf(item -> item.getSeller().equals(username));
            messages.removeIf(m -> m.getSender().equals(username) || m.getReceiver().equals(username));
            users.remove(toRemove);
            return true;
        }
        return false;
    }

    public synchronized User getUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean login(String username, String password) {
        User u = getUser(username);
        return u != null && u.getPassword().equals(password);
    }

    /**
     * Creates a new item in the user's inventory only (not listed for sale).
     */
    @Override
    public synchronized boolean addItem(String name, double cost, String sellerUsername) {
        User seller = getUser(sellerUsername);
        if (seller == null) return false;
        Item newItem = new Item(name, cost, sellerUsername);
        seller.addOwnedItem(newItem);
        ownedItems.add(newItem);
        return true;
    }

    /**
     * Explicitly list an owned item for sale.
     */
    public synchronized boolean sellItem(String username, String itemName) {
        User u = getUser(username);
        if (u == null) return false;
        ArrayList<Item> ownerItems = u.getOwnedItems();
        for (Item ownerItem : ownerItems) {
            if (ownerItem.getName().equals(itemName) && !ownerItem.isSellable()) {
                ownerItem.setSellable(true);
                items.add(ownerItem);
                ownedItems.remove(ownerItem);
                u.removeOwnedItem(ownerItem);
                return true;
            }
        }
        return false;
    }

    /**
     * Unsell an item: remove from marketplace and return to inventory.
     */
    public synchronized boolean unsellItem(String username, String itemName) {
        User u = getUser(username);
        if (u == null) return false;
        for (Item items1: items) {
            if (items1.getName().equals(itemName) && items1.getSeller().equals(username)
                && items1.isSellable()) {
                items1.setSellable(false);
                items.remove(items1);
                ownedItems.add(items1);
                u.addOwnedItem(items1);
                return true;
            }
        }
        return false;
    }

    /**
     * Only returns items currently listed for sale.
     */
    public synchronized ArrayList<Item> getItems() {
        ArrayList<Item> forSale = new ArrayList<>();
        for (Item i : items) {
            if (i.isSellable()) {
                forSale.add(i);
            }
        }
        return forSale;
    }

    /**
     * Finds only sellable items by name (case-insensitive).
     */
    @Override
    public synchronized Item searchSoldItem(String name) {
        for (Item i : items) {
            if (i.isSellable() && i.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return null;
    }

    public synchronized Item searchOwnedItem(String name) {
        for (Item i : ownedItems) {
            if (i.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }

        for (Item i : items) {
            if (i.isSellable() && i.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean deleteItem(String name) {
        Item toRemove = null;
        for (Item i : ownedItems) {
            if (i.getName().equals(name)) {
                toRemove = i;
                break;
            }
        }
        if (toRemove != null) {
            ownedItems.remove(toRemove);
            User seller = getUser(toRemove.getSeller());
            if (seller != null) {
                seller.removeOwnedItem(toRemove);
            }
            return true;
        }
        return false;
    }

    public void changeItemPrice(Item item, double newPrice) {
        for (Item i : items) {
            if (i.getName().equals(item.getName())) {
                i.setCost(newPrice);
                break;
            }
        }

        for (Item i : ownedItems) {
            if (i.getName().equals(item.getName())) {
                i.setCost(newPrice);
                break;
            }
        }
    }

    @Override
    public synchronized void sendMessage(String sender, String receiver, String message) {
        User s = getUser(sender);
        User r = getUser(receiver);
        if (s != null && r != null) {
            if (!s.getMessageUsernameList().contains(receiver)) s.addMessageUsername(receiver);
            if (!r.getMessageUsernameList().contains(sender)) r.addMessageUsername(sender);
        }
        messages.add(new Message(sender, receiver, message));
    }

    @Override
    public synchronized ArrayList<Message> getSingleUserMessage(String username) {
        User u = getUser(username);
        ArrayList<Message> out = new ArrayList<>();
        if (u == null) return out;
        for (Message m : messages) {
            if (m.getSender().equals(username) || m.getReceiver().equals(username)) {
                out.add(m);
            }
        }
        return out;
    }

    public synchronized ArrayList<String> getMessageUserList(String username) {
        User u = getUser(username);
        return u == null ? new ArrayList<>() : u.getMessageUsernameList();
    }

    @Override
    public synchronized ArrayList<Message> getSenderToReceiverMessage(String sender, String receiver) {
        ArrayList<Message> convo = new ArrayList<>();
        for (Message m : messages) {
            if ((m.getSender().equals(sender) && m.getReceiver().equals(receiver)) ||
                    (m.getSender().equals(receiver) && m.getReceiver().equals(sender))) {
                convo.add(m);
            }
        }
        return convo;
    }

    @Override
    public synchronized ArrayList<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    @Override
    public synchronized void processTransaction(User buyer, User seller, Item boughtItem) {
        if (buyer.getBalance() < boughtItem.getCost()) {
            System.out.println("Transaction invalid. Buyer balance is less than item cost.");
        } else {
            buyer.setBalance(buyer.getBalance() - boughtItem.getCost());
            seller.setBalance(seller.getBalance() + boughtItem.getCost());
            buyer.addOwnedItem(boughtItem);
            seller.removeOwnedItem(boughtItem);
            boughtItem.setSellable(false);
            boughtItem.setSeller(buyer.getUsername());
            items.remove(boughtItem);
        }
    }
}