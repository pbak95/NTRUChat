package communication;

/**
 * Created by Patryk on 30.03.2018.
 */
public enum Protocol {
    INFO_CONNECTED, //initial message to client to say that it connects successfully
    INFO_REGISTER, //initial message from client to register itself
    INFO_LOGOUT,
    INIT_CONVERSATION, //get cipher parameters
    INIT_CONVERSATION_ACK, //return cipher parameters
    NEW_CONVERSATION, //new conversation with cipher parameters
    CONVERSATION, //basic conversation messages
    LIST_FRIENDS, //get online friends
    ERROR_MESSAGE_NOT_SENT,
    ERROR_NO_SUCH_CLIENT_ID
}
