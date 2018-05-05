import com.securityinnovation.jNeo.*;
import com.securityinnovation.jNeo.ntruencrypt.NtruEncryptKey;
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
    private NtruEncryptKey currentPbKey;

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
                    writeMessage(new Message(Protocol.INFO_REGISTER, "", clientID, null));
                } else if (message.getMessageType().equals(Protocol.INFO_REGISTER_ACK)) {
                    //updateOnlineFriends(message.getContent());
                    logger.logMessage("Online friends of user " + clientID);
                    onlineFriends.forEach(logger::logMessage);
                    try {
                        Message response = new Message(Protocol.SEND_PB_KEY, "", clientID, null);
                        response.setKey(NTRU.loadKey(dir + "pubKey" + clientID).getPubKey());
                        writeMessage(response);
                    } catch (NtruException e) {
                        e.printStackTrace();
                    }
                } else if (message.getMessageType().equals(Protocol.ERROR_NO_SUCH_CLIENT_ID)) {
                    //TODO handle it
                    //logger.logMessage(message.getContent());
                } else if (message.getMessageType().equals(Protocol.REQUEST_SEND_ACK)) {
                    //TODO save key for later use
                    if(message.getKey() != null) {
                        logger.logMessage("received: "+new String(message.getKey()));
                        started = true;
                        conversationID = message.getSender();
                        try {
                            currentPbKey = new NtruEncryptKey(message.getKey());
                        } catch (Exception e) {
                            logger.logMessage(e.getMessage());
                        }
                        logger.logMessage("Please type in message content");
                    } else {
                        logger.logMessage("Unable to start conversation, client not exist or not connected");
                    }
                } else if (message.getMessageType().equals(Protocol.CONVERSATION)) {
                    logger.logMessage("Received from: "+message.getSender()+" message: "+new String(message.getContent()));
                    byte[] decrypted = NTRU.decryptMessage(NTRU.loadKey(dir+"privKey"+clientID),message.getContent());
                    logger.logMessage("Decrypted: "+new String(decrypted));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void writeMessage(Message message) {
        try {
            logger.logMessage("Protocol: "+message.getMessageType()+" Sender: " + message.getSender() + " Recipient " + message.getRecipient());
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
                        String input = scanner.nextLine();
                        if(started && conversationID != null) {
                            //TODO encrypt
                            Random random = NTRU.createSeededRandom();
                            byte[] encrypted = NTRU.encryptMessage(currentPbKey, random, input.getBytes());
                            writeMessage(new Message(Protocol.CONVERSATION, conversationID, clientID, encrypted));
                        }
                        else {
                            logger.logMessage("Select client to send message");
                            writeMessage(new Message(Protocol.REQUEST_SEND, input, clientID, null));
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
