
/**
 * Defines the contract for the Marketplace server's lifecycle.
 * Server classes must implement Runnable to handle client connections
 * and provide a method to stop the server gracefully.
 */
public interface ServerInterface {
    /**
     * The main server loop entry point; handles incoming connections.
     * Typically invoked via Thread.start(), which calls this run method.
     */
    void run();

    /**
     * Stops the server, closing the listening socket and exiting the loop.
     */
    void stop();
}
