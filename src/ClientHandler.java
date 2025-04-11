import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static final Database1 db = new Database1();
    private User currentUser = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Welcome to the Marketplace Server!");

            String line;
            while ((line = in.readLine()) != null) {
                handleCommand(line);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleCommand(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];

        switch (command) {
            case "register":
                if (args.length < 3) {
                    out.println("Usage: register <username> <password> <balance>");
                    return;
                }
                try {
                    double balance = Double.parseDouble(args[2]);
                    boolean created = db.addUser(args[0], args[1], balance, new ArrayList<>());
                    out.println(created ? "User registered." : "Username already exists.");
                } catch (NumberFormatException e) {
                    out.println("Invalid balance.");
                }
                break;

            case "login":
                if (args.length < 2) {
                    out.println("Usage: login <username> <password>");
                    return;
                }
                if (db.login(args[0], args[1])) {
                    currentUser = db.getUser(args[0]);
                    out.println("Login successful.");
                } else {
                    out.println("Invalid credentials.");
                }
                break;

            case "logout":
                currentUser = null;
                out.println("Logged out.");
                break;

            case "additem":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                if (args.length < 2) {
                    out.println("Usage: additem <name> <cost>");
                    return;
                }
                try {
                    double cost = Double.parseDouble(args[1]);
                    boolean added = db.addItem(args[0], cost, currentUser.getUsername());
                    out.println(added ? "Item listed: " + args[0] + " for $" + cost : "Item already exists.");
                } catch (NumberFormatException e) {
                    out.println("Invalid cost.");
                }
                break;

            case "listitems":
                for (Item item : db.getItems()) {
                    out.println(item.getName() + " - $" + item.getCost() + " - Seller: " + item.getSeller());
                }
                break;

            case "myitems":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                for (Item item : currentUser.getOwnedItems()) {
                    out.println(item.getName() + " - $" + item.getCost());
                }
                break;

            case "searchitem":
                if (args.length < 1) {
                    out.println("Usage: searchitem <itemname>");
                    return;
                }
                Item found = db.searchItem(args[0]);
                if (found != null) {
                    out.println("Found item: " + found.getName() + ", $" + found.getCost() + ", Seller: " + found.getSeller());
                } else {
                    out.println("Item not found.");
                }
                break;

            case "buy":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                if (args.length < 1) {
                    out.println("Usage: buy <itemname>");
                    return;
                }
                Item itemToBuy = db.searchItem(args[0]);
                if (itemToBuy != null) {
                    User seller = db.getUser(itemToBuy.getSeller());
                    db.processTransaction(currentUser, seller, itemToBuy);
                    out.println("Transaction processed.");
                } else {
                    out.println("Item not found.");
                }
                break;

            case "getbalance":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                User user = db.getUser(currentUser.getUsername());
                double balance = user.getBalance();
                out.println(balance);
                break;

            case "deleteitem":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                if (args.length < 1) {
                    out.println("Usage: deleteitem <itemname>");
                    return;
                }
                boolean deleted = db.deleteItem(args[0]);
                out.println(deleted ? "Item deleted." : "Item not found.");
                break;

            case "exit":
                out.println("Goodbye!");
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                break;

            case "sendmessage":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                if (args.length < 2) {
                    out.println("Usage: sendmessage <receiver> <message>");
                    return;
                }
                String appendMessages = args[1];
                if (args.length > 2) {
                    for (int i = 2; i < args.length; i++) {
                        appendMessages += " " + args[i];
                    }
                }
                db.sendMessage(currentUser.getUsername(), args[0], appendMessages);
                out.println("Message sent to " + args[0]);
                break;

            case "viewuserlist":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                ArrayList<String> users = db.getMessageUserList(currentUser.getUsername());
                for (String m : users) {
                    out.println(m);
                }
                break;

            case "viewconversation":
                if (currentUser == null) {
                    out.println("Please login first.");
                    return;
                }
                if (args.length < 1) {
                    out.println("Usage: viewconversation <username>");
                    return;
                }
                ArrayList<Message> convo = db.getSenderToReceiverMessage(currentUser.getUsername(), args[0]);
                for (Message m : convo) {
                    out.println(m);
                }
                break;

            default:
                out.println("Unknown command: " + command);
        }
    }
}