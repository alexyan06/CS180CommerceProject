import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler2 extends Thread {
    private Socket socket;
    private Database1 db;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler2(Socket socket, Database1 db) {
        this.socket = socket;
        this.db = db;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Welcome! Enter commands:");
            String input;
            while ((input = in.readLine()) != null) {
                String response = processCommand(input);
                out.println(response);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String processCommand(String input) {
        String[] parts = input.split(" ");
        String command = parts[0];

        switch (command) {
            case "register":
                if (parts.length < 5) {
                    return "Incorrect usage, should be register <username> <password> <balance> <type>";
                }
                boolean success = db.addUser(parts[1], parts[2], Double.parseDouble(parts[3]), Boolean.parseBoolean(parts[4]));
                return success ? "User registered successfully!" : "Username Taken!";
            case "login":
                User user = db.getUser(parts[1]);
                if (user != null && user.getPassword().equals(parts[2])) {
                    return "Login successful!";
                }
                return "Incorrect username or password";
            case "list-item":
                ArrayList<Item> items = db.getItems();
                if (items.isEmpty()) {
                    return "No items found";
                } else {
                    return items.toString();
                }
            case "add-item":
                if (parts.length < 4) {
                    return "Incorrect usage, should be add-item, item, price, seller";
                } else {
                    db.addItem(parts[1], Double.parseDouble(parts[2]), parts[3]);
                    return "Items added successfully!";
                }
            case:
        }
    }
}
