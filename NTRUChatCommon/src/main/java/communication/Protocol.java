package communication;

/**
 * Created by Patryk on 30.03.2018.
 */
public enum Protocol {
    INFO_CONNECTED, //initial message to client to say that it connects successfully
    INFO_REGISTER, //initial message from client to register itself
    INFO_REGISTER_ACK,
    INFO_LOGOUT,
    SEND_PB_KEY,
    REQUEST_SEND,
    REQUEST_SEND_ACK,
    CONVERSATION, //basic conversation messages
    LIST_FRIENDS, //get online friends
    ERROR_MESSAGE_NOT_SENT,
    ERROR_NO_SUCH_CLIENT_ID
}
