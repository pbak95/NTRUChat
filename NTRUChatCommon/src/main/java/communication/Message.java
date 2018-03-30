package communication;

/**
 * Created by Patryk on 30.03.2018.
 */
public class Message {

    private Protocol messageType;
    private String content;
    private String recipient;
    private String sender;

    public Message(Protocol messageType, String recipient, String sender, String content) {
        this.messageType = messageType;
        this.content = content;
        this.recipient = recipient;
        this.sender = sender;
    }

    public Protocol getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getRecipient() {
        return recipient;
    }
}
