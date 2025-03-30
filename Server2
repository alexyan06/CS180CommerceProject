import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server2 {
    private static final int PORT = 12345;
    private static Database1 db = new Database1();

    public static void main(String[] args) {
        try (ServerSocket sS = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT + "...");

            while(true) {
                Socket clientSocket = sS.accept();
                System.out.println("New client connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                new ClientHandler2(clientSocket, db).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
