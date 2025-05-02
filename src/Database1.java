import java.util.ArrayList;
import java.util.Iterator;

/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * Database1 manages users, items, and messages in-memory during server runtime.
 * All operations are synchronized to ensure thread safety across multiple clients.
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Database1 implements DatabaseInterface {
    /** All registered users. */
    private ArrayList<User> users = new ArrayList<>();
    /** Items currently listed for sale. */
    private ArrayList<Item> items = new ArrayList<>();
    /** All direct messages exchanged between users. */
    private ArrayList<Message> messages = new ArrayList<>();
    /** Items that are owned but not listed for sale. */
    private ArrayList<Item> ownedItems = new ArrayList<>();

    /**
     * Adds a new user if the username is not already taken.
     * @param username desired username
     * @param password desired password
     * @param balance starting balance for the new account
     * @param messageList initial (empty) list of message contacts
     * @return true if registration succeeded, false if username exists
     */
    @Override
    public synchronized boolean addUser(String username, String password,
                                        double balance, ArrayList<String> messageList) {
        // Check for existing user
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return false;
            }
        }
        // Add new user to list
        users.add(new User(username, password, balance, messageList));
        return true;
    }

    /**
     * Deletes a user and cleans up their items and messages.
     * @param username name of user to delete
     * @param password password for authentication
     * @return true if deletion succeeded, false otherwise
     */
    @Override
    public synchronized boolean deleteUser(String username, String password) {
        User toRemove = getUser(username);
        // Verify credentials
        if (toRemove != null && toRemove.getPassword().equals(password)) {
            // Remove all items they listed
            items.removeIf(item -> item.getSeller().equals(username));
            // Remove all messages sent to or from them
            messages.removeIf(m -> m.getSender().equals(username) || m.getReceiver().equals(username));
            // Remove user record
            users.remove(toRemove);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a user by username.
     * @param username the username to look up
     * @return the User object, or null if not found
     */
    public synchronized User getUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Authenticates a user by verifying username and password.
     * @param username name to authenticate
     * @param password password to check
     * @return true if credentials match an existing user
     */
    @Override
    public synchronized boolean login(String username, String password) {
        User u = getUser(username);
        return u != null && u.getPassword().equals(password);
    }

    /**
     * Adds a new item to a user's owned inventory (not listed for sale).
     * @param name item name
     * @param cost item price
     * @param sellerUsername owner of the new item
     * @return true if successful, false if user not found
     */
    @Override
    public synchronized boolean addItem(String name, double cost, String sellerUsername) {
        User seller = getUser(sellerUsername);
        if (seller == null) return false;
        Item newItem = new Item(name, cost, sellerUsername);
        // Add to user's owned items and global owned list
        seller.addOwnedItem(newItem);
        ownedItems.add(newItem);
        return true;
    }

    /**
     * Marks an owned item as sellable and moves it to marketplace.
     * @param username who owns the item
     * @param itemName name of the item
     * @return true if item successfully listed, false otherwise
     */
    public synchronized boolean sellItem(String username, String itemName) {
        User u = getUser(username);
        if (u == null) return false;
        ArrayList<Item> ownerItems = u.getOwnedItems();
        for (Item ownerItem : ownerItems) {
            // Only list if matching name and not already sellable
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
     * Unlists an item from sale and returns it to the owner's inventory.
     * @param username who previously listed the item
     * @param itemName name of the item
     * @return true if unlisted successfully, false otherwise
     */
    public synchronized boolean unsellItem(String username, String itemName) {
        User u = getUser(username);
        if (u == null) return false;
        for (Item listed : items) {
            if (listed.getName().equals(itemName) &&
                listed.getSeller().equals(username) &&
                listed.isSellable()) {
                listed.setSellable(false);
                items.remove(listed);
                ownedItems.add(listed);
                u.addOwnedItem(listed);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of items currently available for sale.
     * @return list of sellable items
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
     * Searches the marketplace for a sellable item by name (case-insensitive).
     * @param name item name to search
     * @return the Item if found, null otherwise
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

    /**
     * Searches both owned and listed items by name.
     * @param name item name to search
     * @return the Item if found, null otherwise
     */
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

    /**
     * Deletes an owned (but unlisted) item permanently.
     * @param name item name to delete
     * @return true if deletion succeeded, false otherwise
     */
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

    /**
     * Updates the price of an item in both marketplace and inventory.
     * @param item the item to modify
     * @param newPrice the new cost value
     */
    public void changeItemPrice(Item item, double newPrice) {
        // Update listed items
        for (Item i : items) {
            if (i.getName().equals(item.getName())) {
                i.setCost(newPrice);
                break;
            }
        }
        // Update unlisted owned items
        for (Item i : ownedItems) {
            if (i.getName().equals(item.getName())) {
                i.setCost(newPrice);
                break;
            }
        }
    }

    /**
     * Sends a direct message from one user to another and records contacts.
     * @param sender username of sender
     * @param receiver username of receiver
     * @param message the message text
     */
    @Override
    public synchronized void sendMessage(String sender, String receiver, String message) {
        User s = getUser(sender);
        User r = getUser(receiver);
        if (s != null && r != null) {
            // Track each other in contact lists
            if (!s.getMessageUsernameList().contains(receiver)) s.addMessageUsername(receiver);
            if (!r.getMessageUsernameList().contains(sender)) r.addMessageUsername(sender);
        }
        messages.add(new Message(sender, receiver, message));
    }

    /**
     * Retrieves all messages sent or received by a single user.
     * @param username user whose messages are requested
     * @return list of matching Message objects
     */
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

    /**
     * Retrieves the list of usernames with whom a user has exchanged messages.
     * @param username the user in question
     * @return list of contact usernames
     */
    public synchronized ArrayList<String> getMessageUserList(String username) {
        User u = getUser(username);
        return u == null ? new ArrayList<>() : u.getMessageUsernameList();
    }

    /**
     * Retrieves the full conversation between two users.
     * @param sender one participant
     * @param receiver the other participant
     * @return ordered list of Message objects between the two users
     */
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

    /**
     * Returns a copy of all messages in the system.
     * @return list of all Message objects
     */
    @Override
    public synchronized ArrayList<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * Handles money transfer, ownership update, and listing removal when an item is purchased.
     * @param buyer the User buying the item
     * @param seller the User selling the item
     * @param boughtItem the Item being transferred
     */
    @Override
    public synchronized void processTransaction(User buyer, User seller, Item boughtItem) {
        // Validate buyer has sufficient funds
        if (buyer.getBalance() < boughtItem.getCost()) {
            System.out.println("Transaction invalid. Buyer balance is less than item cost.");
        } else {
            // Adjust balances
            buyer.setBalance(buyer.getBalance() - boughtItem.getCost());
            seller.setBalance(seller.getBalance() + boughtItem.getCost());
            // Transfer ownership
            buyer.addOwnedItem(boughtItem);
            seller.removeOwnedItem(boughtItem);
            // Mark item as no longer sellable and update seller name
            boughtItem.setSellable(false);
            boughtItem.setSeller(buyer.getUsername());
            // Remove from marketplace listings
            items.remove(boughtItem);
        }
    }
}
