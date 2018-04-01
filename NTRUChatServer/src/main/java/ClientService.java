import communication.Message;
import communication.Protocol;
import logger.SimpleLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Patryk on 30.03.2018.
 */
public class ClientService implements Runnable {

    private Socket clientSocket;
    private ChatServer server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private SimpleLogger logger;



    public ClientService(Socket clientSocket, ChatServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        logger = SimpleLogger.getInstance();
    }

    @Override
    public void run() {
        initializeStreams();
        logger.logMessage("New ClientService running");
        writeMessage(new Message(Protocol.INFO_CONNECTED, "", "", ""));
        while (true) {
            try {
                Message message = (Message) inputStream.readObject();

                if (message.getMessageType().equals(Protocol.INFO_REGISTER)) {
                    boolean isConnected = server.connectClient(message.getSender(), this);
                    if (!isConnected) {
                        writeMessage(new Message(Protocol.ERROR_NO_SUCH_CLIENT_ID, "", "",
                                "There is not client with such identifier"));
                    } else {
                        writeMessage(new Message(Protocol.INFO_REGISTER_ACK, message.getSender(), "",
                                server.getClientFriends(message.getSender())));
                    }
                } else if (message.getMessageType().equals(Protocol.CONVERSATION)) {
                    boolean isSent = server.sendMessageToClient(message);
                    if (!isSent) {
                        writeMessage(new Message(Protocol.ERROR_MESSAGE_NOT_SENT, "", "",
                                "Message not sent, your friend is offline"));
                    }
                } else if (message.getMessageType().equals(Protocol.INFO_LOGOUT)) {
                    server.logout(message);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public synchronized void writeMessage(Message message) {
        try {
            outputStream.reset();
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeStreams() {
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeStreams() {
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
