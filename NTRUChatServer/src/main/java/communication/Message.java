package main.java.communication;

/**
 * Created by Patryk on 30.03.2018.
 */
public class Message {

    private Protocol messageType;
    private String content;

    public Message(Protocol messageType, String content) {
        this.messageType = messageType;
        this.content = content;
    }

    public Protocol getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }
}
