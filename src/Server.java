import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Server implements Runnable, ServerInterface {
    public static final int PORT = 12345;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    /** Entry point: spins up a thread running this Server instance. */
    public static void main(String[] args) {
        Server server = new Server();
        Thread srvThread = new Thread(server, "MarketplaceServer");
        srvThread.start();
    }

    /** The Runnable.run that does the accept‐loop. */
    @Override
    public void run() {
        running = true;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");
                // each client in its own thread
                new Thread(new ClientHandler(clientSocket), "ClientHandler").start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /** Shuts down the server socket and breaks out of accept loop. */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
                //intentionally blank
            }
        }
        System.out.println("Server stopped.");
    }
}