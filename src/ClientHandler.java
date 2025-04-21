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
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static Database1 db = new Database1();
    private User currentUser = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
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

    /**
     * Parse and execute a single command from the client.
     */
    private void handleCommand(String input) {
        String[] parts = input.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];
        switch (cmd) {
            case "register":
                if (args.length < 3) { out.println("Usage: register <username> <password> <balance>"); break; }
                try {
                    double bal = Double.parseDouble(args[2]);
                    boolean ok = db.addUser(args[0], args[1], bal, new ArrayList<>());
                    out.println(ok ? "User registered." : "Username already exists.");
                } catch (NumberFormatException e) {
                    out.println("Invalid balance.");
                }
                break;

            case "login":
                if (args.length < 2) { out.println("Usage: login <username> <password>"); break; }
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
                if (checkLoggedIn()) break;
                if (args.length<2) { out.println("Usage: additem <name> <cost>"); break; }
                try {
                    double cost = Double.parseDouble(args[1]);
                    boolean ok = db.addItem(args[0], cost, currentUser.getUsername());
                    out.println(ok ? "Item added to inventory." : "Failed to add item.");
                } catch (NumberFormatException e) {
                    out.println("Invalid cost.");
                }
                break;

            case "sellitem":
                if (checkLoggedIn()) break;
                if (args.length<1) { out.println("Usage: sellitem <itemname>"); break; }
                out.println(db.sellItem(currentUser.getUsername(), args[0])
                        ? "Item listed for sale." : "Cannot sell: not in inventory or already listed.");
                break;

            case "unsellitem":
                if (checkLoggedIn()) break;
                if (args.length<1) { out.println("Usage: unsellitem <itemname>"); break; }
                out.println(db.unsellItem(currentUser.getUsername(), args[0])
                        ? "Item removed from sale." : "Cannot unlist: not listed by you.");
                break;

            case "listitems":
                for (Item i : db.getItems()) {
                    out.println(i.getName() + " - $" + String.format("%.2f", i.getCost())
                            + " - Seller: " + i.getSeller());
                }
                break;

            case "myitems":
                if (checkLoggedIn()) break;
                for (Item item : currentUser.getOwnedItems()) {
                    String price = String.format("%.2f", item.getCost());
                    out.println(item.getName() + " - $" + price);
                }
                break;

            case "searchitem":
                if (args.length < 1) {
                    out.println("Usage: searchitem <itemname>");
                } else {
                    Item f = db.searchItem(args[0]);
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
                if (checkLoggedIn()) break;
                if (args.length < 1) {
                    out.println("Usage: buy <itemname>");
                } else {
                    Item toBuy = db.searchItem(args[0]);
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
                if (checkLoggedIn()) break;
                double bal = currentUser.getBalance();
                out.println("$" + String.format("%.2f", bal));
                break;

            case "deleteitem":
                if (checkLoggedIn()) break;
                if (args.length < 1) {
                    out.println("Usage: deleteitem <itemname>");
                } else {
                    Item rem = db.searchItem(args[0]);
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

            case "sendmessage":
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
                if (checkLoggedIn()) break;
                ArrayList<String> us = db.getMessageUserList(currentUser.getUsername());
                if (us.isEmpty()) out.println("No messaging history.");
                else us.forEach(out::println);
                break;

            case "viewconversation":
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
                out.println("Goodbye!");
                try { socket.close(); } catch (IOException ignored) {}
                break;

            default:
                out.println("Unknown command: " + cmd);
        }
    }

    /**
     * Helper: checks login and notifies client, returns true if not logged in.
     */
    private boolean checkLoggedIn() {
        if (currentUser == null) {
            out.println("Please login first.");
            return true;
        }
        return false;
    }
}
