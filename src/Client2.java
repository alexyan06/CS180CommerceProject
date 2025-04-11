import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    public static final String HOST = "localhost";
    public static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println("[SERVER] " + fromServer);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Main thread handles user input
            while (true) {
                String input = scanner.nextLine();
                out.println(input);

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
