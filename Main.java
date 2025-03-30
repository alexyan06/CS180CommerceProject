public class Main {
    public static void main(String[] args) {
        Database1 db = new Database1();

        // Add Users
        db.addUser("Alice", "password123", 100.0, true);
        db.addUser("Bob", "securepass", 50.0, false);

        // Add Items
        db.addItem("Headphones", 50.0, "Bob");

        // Send Messages
        db.sendMessage("Alice", "Bob", "Hi Bob, interested in your headphones?");
        db.sendMessage("Bob", "Alice", "Sure, let's negotiate.");

        // Print Users
        System.out.println("Users:");
        for (String username : new String[]{"Alice", "Bob"}) {
            User user = db.getUser(username);
            System.out.println(user);
        }

        // Print Items
        System.out.println("\nItems for Sale:");
        for (Item item : db.getItems()) {
            System.out.println(item);
        }

        // Print Messages
        System.out.println("\nMessages:");
        for (Message msg : db.getMessages()) {
            System.out.println(msg);
        }
    }
}
