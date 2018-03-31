import communication.Message;
import communication.Protocol;
import logger.SimpleLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Patryk on 31.03.2018.
 */
public class ChatClient implements Runnable {

    // Server port
    private final int SERVER_PORT = 2222;
    private final String HOST = "localhost";
    private static Scanner inputLine;


    private Socket clientSocket;
    /** object to send */
    private ObjectOutputStream outputStream;
    /** object to read */
    private ObjectInputStream inputStream;
    private SimpleLogger logger;
    private String clientID;
    private List<String> onlineFriends;

    public ChatClient() {
        logger = SimpleLogger.getInstance();
        initializeClient();
        clientID = "Alice"; //temporary to test, default, read this from console while client starting
        run();
    }

    @Override
    public void run() {
        initializeStreams();
        logger.logMessage("Client running");
        while (true) {
            try {
                Message message = (Message) inputStream.readObject();
                if (message.getMessageType().equals(Protocol.INFO_CONNECTED)) {
                   writeMessage(new Message(Protocol.INFO_REGISTER, "", clientID, ""));
                } else if (message.getMessageType().equals(Protocol.INFO_REGISTER_ACK)) {
                    updateOnlineFriends(message.getContent());
                } else if(message.getMessageType().equals(Protocol.ERROR_NO_SUCH_CLIENT_ID)) {
                    //TODO handle it
                    logger.logMessage(message.getContent());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeMessage(Message message) {
        try {
            outputStream.reset();
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateOnlineFriends(String friendsInStr) {
        String[] friendsArr = friendsInStr.split("\\|");
        onlineFriends = Arrays.asList(friendsArr);
    }

    private void initializeClient() {
        logger.logMessage("Starting client");
        try {
            clientSocket = new Socket(HOST, SERVER_PORT);
            onlineFriends = new ArrayList<>();
            logger.logMessage("Client started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeStreams() {
        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
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

    public static void consoleHandler() {

    }

    public static void main(String[] args) {
        new ChatClient();
    }
}
