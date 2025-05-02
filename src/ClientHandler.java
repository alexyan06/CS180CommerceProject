import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class ClientHandler implements Runnable {
    // Underlying socket connected to the client
    private Socket socket;
    // Reader for incoming client messages
    private BufferedReader in;
    // Writer for outgoing responses
    private PrintWriter out;
    // Shared in-memory database instance
    private static Database1 db = new Database1();
    // Currently logged-in user for this handler
    private User currentUser = null;

    /**
     * Constructs a handler for the given client socket.
     * @param socket the client's socket connection
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Main loop: sets up streams, greets client, and processes commands until disconnect.
     */
    @Override
    public void run() {
        try {
            // Initialize input/output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send welcome message
            out.println("Welcome to the Marketplace Server!");

            String line;
            // Read and handle each incoming line until client disconnects
            while ((line = in.readLine()) != null) {
                handleCommand(line);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            // Clean up socket on termination
            try {
                socket.close();
            } catch (IOException ignored) {
                // intentionally blank
            }
        }
    }

    /**
     * Parses and executes a single command from the client.
     * @param input raw command line text
     */
    private void handleCommand(String input) {
        // Split command and arguments
        String[] parts = input.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];

        switch (cmd) {
            case "register":
                // Create a new user with initial balance
                if (args.length < 3) {
                    out.println("Usage: register <username> <password> <balance>");
                    break;
                }
                try {
                    double bal = Double.parseDouble(args[2]);
                    boolean ok = db.addUser(args[0], args[1], bal, new ArrayList<>());
                    out.println(ok ? "User registered." : "Username already exists.");
                } catch (NumberFormatException e) {
                    out.println("Invalid balance.");
                }
                break;

            case "login":
                // Authenticate existing user
                if (args.length < 2) {
                    out.println("Usage: login <username> <password>");
                    break;
                }
                if (db.login(args[0], args[1])) {
                    currentUser = db.getUser(args[0]);
                    out.println("Login successful.");
                } else {
                    out.println("Invalid credentials.");
                }
                break;

            case "logout":
                // Clear current session
                currentUser = null;
                out.println("Logged out.");
                break;

            case "additem":
                // Add a new item to user's inventory
                if (checkLoggedIn()) break;
                if (args.length < 2) {
                    out.println("Usage: additem <name> <cost>");
                    break;
                }
                try {
                    double cost = Double.parseDouble(args[1]);
                    boolean ok = db.addItem(args[0], cost, currentUser.getUsername());
                    out.println(ok ? "Item added to inventory." : "Failed to add item.");
                } catch (NumberFormatException e) {
                    out.println("Invalid cost.");
                }
                break;

            case "sellitem":
                // List an owned item for sale
                if (checkLoggedIn()) break;
                if (args.length < 1) {
                    out.println("Usage: sellitem <itemname>");
                    break;
                }
                out.println(db.sellItem(currentUser.getUsername(), args[0])
                        ? "Item listed for sale." : "Cannot sell: not in inventory or already listed.");
                break;

            case "unsellitem":
                // Unlist an item, return it to inventory
                if (checkLoggedIn()) break;
                if (args.length < 1) {
                    out.println("Usage: unsellitem <itemname>");
                    break;
                }
                out.println(db.unsellItem(currentUser.getUsername(), args[0])
                        ? "Item removed from sale." : "Cannot unlist: not listed by you.");
                break;

            case "listitems":
                // Display all items currently for sale
                for (Item i : db.getItems()) {
                    out.println(i.getName() + " - $" + String.format("%.2f", i.getCost())
                            + " - Seller: " + i.getSeller());
                }
                break;

            case "myitems":
                // Show items in the current user's inventory
                if (checkLoggedIn()) break;
                for (Item item : currentUser.getOwnedItems()) {
                    String price = String.format("%.2f", item.getCost());
                    out.println(item.getName() + " - $" + price);
                }
                break;

            case "searchitem":
                // Search for a specific listed item
                if (args.length < 1) {
                    out.println("Usage: searchitem <itemname>");
                } else {
                    Item f = db.searchSoldItem(args[0]);
                    if (f != null) {
                        String price = String.format("%.2f", f.getCost());
                        out.println("Found item: " + f.getName()
                                + ", $" + price
                                + ", Seller: " + f.getSeller());
                    } else {
                        out.println("Item not found or not for sale.");
                    }
                }
                break;

            case "buy":
                // Purchase a listed item
                if (checkLoggedIn()) break;
                if (args.length < 1) {
                    out.println("Usage: buy <itemname>");
                } else {
                    Item toBuy = db.searchSoldItem(args[0]);
                    if (toBuy != null) {
                        User seller = db.getUser(toBuy.getSeller());
                        db.processTransaction(currentUser, seller, toBuy);
                        out.println("Transaction processed.");
                    } else {
                        out.println("Item not found.");
                    }
                }
                break;

            case "getbalance":
                // Show the current user's balance
                if (checkLoggedIn()) break;
                double bal = currentUser.getBalance();
                out.println("$" + String.format("%.2f", bal));
                break;

            case "deleteitem":
                // Permanently remove an owned listing
                if (checkLoggedIn()) break;
                if (args.length < 1) {
                    out.println("Usage: deleteitem <itemname>");
                } else {
                    Item rem = db.searchOwnedItem(args[0]);
                    if (rem == null) {
                        out.println("Item not found.");
                    } else if (!rem.getSeller().equals(currentUser.getUsername())) {
                        out.println("You can only delete your own items.");
                    } else {
                        boolean d = db.deleteItem(args[0]);
                        out.println(d ? "Item deleted." : "Item could not be deleted.");
                    }
                }
                break;

            case "changeitemprice":
                // Update the price of a listed or owned item
                if (checkLoggedIn()) break;
                if (args.length < 2) {
                    out.println("Usage: changeitemprice <itemname> <newprice>");
                } else {
                    Item item1 = db.searchSoldItem(args[0]);
                    Item item2 = db.searchOwnedItem(args[0]);
                    if (item2 == null && item1 == null) {
                        out.println("Item not found.");
                    } else {
                        // Handle sold listings first
                        if (item1 != null) {
                            try {
                                double newPrice = Double.parseDouble(args[1]);
                                if (!item1.getSeller().equals(currentUser.getUsername())) {
                                    out.println("You can only change your own items.");
                                } else {
                                    db.changeItemPrice(item1, newPrice);
                                    out.println("Item changed to $" + String.format("%.2f", newPrice));
                                }
                            } catch (NumberFormatException e) {
                                out.println("Invalid price.");
                            }
                        // Handle items not yet listed
                        } else {
                            try {
                                double newPrice = Double.parseDouble(args[1]);
                                if (!item2.getSeller().equals(currentUser.getUsername())) {
                                    out.println("You can only change your own items.");
                                } else {
                                    db.changeItemPrice(item2, newPrice);
                                    out.println("Item changed to $" + String.format("%.2f", newPrice));
                                }
                            } catch (NumberFormatException e) {
                                out.println("Invalid price.");
                            }
                        }
                    }
                }
                break;

            case "sendmessage":
                // Send a direct message to another user
                if (checkLoggedIn()) break;
                if (args.length < 2) {
                    out.println("Usage: sendmessage <receiver> <message>");
                } else {
                    String msg = args[1];
                    for (int i = 2; i < args.length; i++) msg += " " + args[i];
                    db.sendMessage(currentUser.getUsername(), args[0], msg);
                    out.println("Message sent to " + args[0]);
                }
                break;

            case "viewuserlist":
                // List all users this client has messaged
                if (checkLoggedIn()) break;
                ArrayList<String> us = db.getMessageUserList(currentUser.getUsername());
                if (us.isEmpty()) out.println("No messaging history.");
                else us.forEach(out::println);
                break;

            case "viewconversation":
                // Retrieve conversation thread with another user
                if (checkLoggedIn()) break;
                if (args.length < 1) {
                    out.println("Usage: viewconversation <username>");
                } else {
                    for (Message m : db.getSenderToReceiverMessage(currentUser.getUsername(), args[0])) {
                        out.println(m);
                    }
                }
                break;

            case "exit":
                // Client requested disconnect
                out.println("Goodbye!");
                try { socket.close(); } catch (IOException ignored) {}
                break;

            default:
                // Unrecognized command
                out.println("Unknown command: " + cmd);
        }
    }

    /**
     * Verifies that a user is logged in before executing restricted commands.
     * @return true if not logged in (and sent prompt), false otherwise
     */
    private boolean checkLoggedIn() {
        if (currentUser == null) {
            out.println("Please login first.");
            return true;
        }
        return false;
    }
}
