package communication;

import java.io.Serializable;

/**
 * Created by Patryk on 30.03.2018.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private Protocol messageType;
    private String content;
    private byte[] key;
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

    public String getSender() {
        return sender;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }
}
