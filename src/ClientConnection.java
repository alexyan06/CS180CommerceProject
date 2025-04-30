import java.io.*;
import java.net.Socket;

/**
 * One per GUI window – maintains a socket, sends commands, and delivers each
 * server line to a callback.  No daemon threads are used; the listener thread
 * is stopped explicitly in close().
 */
public class ClientConnection {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Thread listener;

    /** Functional interface for async messages from the server. */
    public interface LineHandler { void onLine(String line); }

    public ClientConnection(String host, int port, LineHandler handler) throws IOException {
        socket = new Socket(host, port);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        listener = new Thread(() -> {
            try {
                String s;
                while ((s = in.readLine()) != null) handler.onLine(s);
            } catch (IOException ignored) {
                // socket closed or interrupted
            }
        }, "ServerListener");
        listener.start();
    }

    /** Send one command line to the server. */
    public void send(String cmd) { out.println(cmd); }

    /** Graceful shutdown: close socket and wait for listener to exit. */
    public void close() {
        try { socket.close(); } catch (IOException ignored) {}

    }
}
