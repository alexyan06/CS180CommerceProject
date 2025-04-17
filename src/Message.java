/**
 * Phase 2 of CS180 Group Project
 *
 * Purdue University -- CS18000 -- Spring 2025
 *
 * @author alexyan06, shivensaxena28, KayshavBhardwaj
 * @version April 6, 2025
 */
public class Message implements MessageInterface {
    private String sender;
    private String receiver;
    private String message;

    public Message(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return sender + " " + receiver + " " + message;
    }
}
