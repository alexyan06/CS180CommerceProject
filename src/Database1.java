import java.util.ArrayList;

/**
 * Phase 1 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, wang6377, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Database1 implements DatabaseInterface {
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<Message> messages = new ArrayList<Message>();

    public synchronized boolean addUser(String username,
                                        String password, double balance, ArrayList<String> messageUser) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }
        users.add(new User(username, password, balance, messageUser));

        return true;
    }

    // ✅ DELETE USER + CLEANUP THEIR LISTINGS & MESSAGES
    public synchronized boolean deleteUser(String username, String password) {
        User userToDelete = getUser(username);
        if (userToDelete != null && userToDelete.getPassword().equals(password)) {

            // 1. Remove their items from marketplace
            items.removeIf(item -> item.getSeller().equals(username));

            // 2. Remove messages involving this user
            messages.removeIf(m -> m.getSender().equals(username) || m.getReceiver().equals(username));

            // 3. Remove user
            users.remove(userToDelete);
            return true;
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

        Item newItem = new Item(name, cost, seller);
        items.add(newItem);

        User sellerUser = getUser(seller);
        if (sellerUser != null) {
            sellerUser.addOwnedItem(newItem);
        }
        return true;
    }

    public synchronized boolean deleteItem(String name) {
        Item itemToRemove = null;

        for (Item item : items) {
            if (item.getName().equals(name)) {
                itemToRemove = item;
                break;
            }
        }

        if (itemToRemove != null) {
            items.remove(itemToRemove); // Remove from marketplace

            // Remove from seller's ownedItems
            User seller = getUser(itemToRemove.getSeller());
            if (seller != null) {
                seller.removeOwnedItem(itemToRemove);
            }

            return true;
        }

        return false;
    }

    public synchronized ArrayList<Item> getItems() {
        return new ArrayList<>(items);
    }

    // ✅ LOGIN VALIDATION
    public synchronized boolean login(String username, String password) {
        User user = getUser(username);
        return user != null && user.getPassword().equals(password);
    }

    // ✅ SEARCH ITEM BY NAME (case-insensitive)
    public synchronized Item searchItem(String name) {
        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }



    public synchronized void sendMessage(String senderUsername, String receiverUsername, String message) {
        User senderUser = getUser(senderUsername);
        User receiverUser = getUser(receiverUsername);

        if (senderUser != null && receiverUser != null) {
            if (!senderUser.getMessageUsernameList().contains(receiverUsername)) {
                senderUser.addMessageUsername(receiverUsername);
            }
            if (!receiverUser.getMessageUsernameList().contains(senderUsername)) {
                receiverUser.addMessageUsername(senderUsername);
            }
        }
        messages.add(new Message(senderUsername, receiverUsername, message));
    }

    public synchronized ArrayList<Message> getSingleUserMessage(String username) {
        User user = getUser(username);
        if (user == null) {
            return new ArrayList<>();
        }
        // Retrieve the list of contacts the user has messaged with
        ArrayList<String> contacts = user.getMessageUsernameList();
        ArrayList<Message> userMessages = new ArrayList<>();

        // Iterate over the global list of messages
        for (Message message : messages) {
            // Check if the message involves the user and the other party is in their contacts
            if (message.getSender().equals(username) && contacts.contains(message.getReceiver()) ||
                    message.getReceiver().equals(username) && contacts.contains(message.getSender())) {
                userMessages.add(message);
            }
        }
        return userMessages;
    }

    public synchronized ArrayList<String> getMessageUserList(String username) {
        User user = getUser(username);
        if (user == null) {
            return new ArrayList<>();
        }
        return user.getMessageUsernameList();
    }

    public synchronized ArrayList<Message> getSenderToReceiverMessage(String sender, String receiver) {
        ArrayList<Message> conversation = new ArrayList<>();
        for (Message message : messages) {
            if ((message.getSender().equals(sender) && message.getReceiver().equals(receiver)) ||
                    (message.getSender().equals(receiver) && message.getReceiver().equals(sender))) {
                conversation.add(message);
            }
        }
        return conversation;
    }

    public synchronized ArrayList<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public String displayThread(String userA, String userB) {
        ArrayList<Message> convo = getSenderToReceiverMessage(userA, userB);
        StringBuilder sb = new StringBuilder();
        for (Message m : convo) {
            sb.append(m.toString()).append("\n");
        }
        return sb.toString();
    }


    public synchronized void processTransaction(User buyer, User seller, Item boughtItem) {
        if (buyer.getBalance() < boughtItem.getCost()) {
            System.out.println("Transaction invalid. Buyer balance is less than item cost.");
        } else {
            buyer.setBalance(buyer.getBalance() - boughtItem.getCost());
            seller.setBalance(seller.getBalance() + boughtItem.getCost());

            buyer.addOwnedItem(boughtItem);
            seller.removeOwnedItem(boughtItem);

            items.remove(boughtItem);
        }
    }
}
