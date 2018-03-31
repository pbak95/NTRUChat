import communication.Message;
import logger.SimpleLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Patryk on 30.03.2018.
 */
public class ChatServer implements Runnable {

    // Server port
    private final int SERVER_PORT = 2222;

    private static final String DB_PATH = "D:\\studia\\RAOI\\NTRUChat\\" +
            "NTRUChat\\NTRUChatServer\\src\\main\\resources\\clientsDB.properties";

    private ServerSocket serverSocket;

    private Map<String, ClientService> connectedClients;

    /*
        this is in memory database, first key is clientID(user), map associated with this key is
        <friendID(user's friend), status(online or not)>
     */
    private ConcurrentHashMap<String, Map<String, Boolean>> inMemoryDatabase;

    private SimpleLogger logger;

    public ChatServer() {
        logger = SimpleLogger.getInstance();
        initializeServer();
        initializeClients();
        run();
    }

    @Override
    public void run() {
        ExecutorService service = Executors.newFixedThreadPool(5);
        try {
            while (true) {
                Socket client = serverSocket.accept();
                logger.logMessage("New connection accepted");
                service.execute(new ClientService(client, this));
                //new Thread(new ClientService(client, this)).start();
                System.out.println("jestem tu czekam na nowe");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean connectClient(String clientID, ClientService clientService) {
        if (!inMemoryDatabase.keySet().contains(clientID))
            return false;
        connectedClients.put(clientID, clientService);
        setClientStatus(clientID, true);
        logger.logMessage("Client " + clientID + " connected");
        return true;
    }

    public synchronized void logout(Message message) {
        String clientID = message.getContent();
        if (inMemoryDatabase.keySet().contains(clientID)) {
            connectedClients.remove(clientID);
            setClientStatus(clientID, false);
            logger.logMessage("Client " + clientID + " disconnected");
        }
    }

    public synchronized boolean sendMessageToClient(Message message) {
        if (!connectedClients.keySet().contains(message.getRecipient())) {
            return false;
        }
        connectedClients.get(message.getRecipient()).writeMessage(message);
        return true;
    }

    /*
        get online friends with which requesting client could chat
     */
    public synchronized String getClientFriends(String clientID) {
        String friendsList = "";
        Map<String, Boolean> friends = inMemoryDatabase.get(clientID);
        for(Map.Entry<String, Boolean> entry : friends.entrySet()) {
            if (entry.getValue())
                friendsList += entry.getKey() + "\\|";
        }
        return friendsList;
    }

    private void setClientStatus(String clientID, boolean status) {
        Map<String, Boolean> friends = inMemoryDatabase.get(clientID);
        for(Map.Entry<String, Boolean> entry : friends.entrySet()) {
            inMemoryDatabase.get(entry.getKey()).put(clientID, status);
        }
    }

    private void initializeServer() {
        logger.logMessage("Starting server on port: " + SERVER_PORT);
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            inMemoryDatabase = new ConcurrentHashMap<>();
            connectedClients = new HashMap<>();
            logger.logMessage("Server started successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeClients() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(DB_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.forEach((client, value) -> {
            String[] clientProperty = client.toString().split("\\.");
            String[] friendsProperty = value.toString().split("\\|");
            Map<String, Boolean> fiendsToDB = new HashMap<>();
            for (int i = 0; i < friendsProperty.length; i++) {
                fiendsToDB.put(friendsProperty[i], false);
            }
            //last string from clientProperty is ID
            inMemoryDatabase.put(clientProperty[clientProperty.length - 1], fiendsToDB);
        });

    }

    public static void main(String args[]) {
        new ChatServer();
    }
}
