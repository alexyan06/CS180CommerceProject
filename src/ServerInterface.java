/**
 * Phase 2 of CS180 Group Project
 *
 * <p>Purdue University -- CS18000 -- Spring 2025</p>
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
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
