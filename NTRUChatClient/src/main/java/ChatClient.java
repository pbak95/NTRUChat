import com.securityinnovation.jNeo.NtruException;
import com.securityinnovation.jNeo.OID;
import com.securityinnovation.jNeo.Random;
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
    private static Scanner scanner;
    private String dir = "NTRUChatClient\\src\\main\\java\\";

    private Socket clientSocket;
    /**
     * object to send
     */
    private ObjectOutputStream outputStream;
    /**
     * object to read
     */
    private ObjectInputStream inputStream;
    private SimpleLogger logger;
    private String clientID;
    private List<String> onlineFriends;
    private boolean started = false;
    private String conversationID;

    public ChatClient(String tmpName) {
        logger = SimpleLogger.getInstance();
        scanner = new Scanner(System.in);
        clientID = tmpName; //temporary to test, default, read this from console while client starting
        initializeClient();
        consoleHandler();
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
                    logger.logMessage("Online friends of user " + clientID);
                    onlineFriends.forEach(logger::logMessage);
                    try {
                        Message response = new Message(Protocol.SEND_PB_KEY, "", clientID, "");
                        response.setKey(NTRU.loadKey(dir + "pubKey" + clientID).getPubKey());
                        writeMessage(response);
                    } catch (NtruException e) {
                        e.printStackTrace();
                    }
                } else if (message.getMessageType().equals(Protocol.ERROR_NO_SUCH_CLIENT_ID)) {
                    //TODO handle it
                    logger.logMessage(message.getContent());
                } else if (message.getMessageType().equals(Protocol.REQUEST_SEND_ACK)) {
                    //TODO save key for later use
                    if(message.getKey() != null) {
                        logger.logMessage("received: "+new String(message.getKey()));
                        started = true;
                        conversationID = message.getSender();
                        logger.logMessage("Please type in message content");
                    } else {
                        logger.logMessage("Unable to start conversation, client not exist or not connected");
                    }
                } else if (message.getMessageType().equals(Protocol.CONVERSATION)) {
                    //TODO decrypt message with private key
                    logger.logMessage("Received from: "+message.getSender()+" message: "+message.getContent());
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
            logger.logMessage("Protocol: "+message.getMessageType()+" Sender: " + message.getSender() + " Recipient " + message.getRecipient()+" Content: "+message.getContent());
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
            setupNTRU();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupNTRU() {
        Random prng = NTRU.createSeededRandom();
        OID oid = NTRU.parseOIDName("ees401ep1");
        try {
            NTRU.setupNtruEncryptKey(prng, oid, (dir + "pubKey" + clientID), (dir + "privKey" + clientID));
        } catch (Exception e) {
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

    public void consoleHandler() {
        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        String input = scanner.next();
                        if(started && conversationID != null) {
                            //TODO encrypt
                            writeMessage(new Message(Protocol.CONVERSATION, conversationID, clientID, input));
                        }
                        else {
                            logger.logMessage("Select client to send message");
                            writeMessage(new Message(Protocol.REQUEST_SEND, input, clientID, ""));
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        inputThread.start();
    }

    public static void main(String[] args) {
        new ChatClient(args[0]);
    }
}
