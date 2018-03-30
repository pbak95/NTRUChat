package main.java;

import main.java.communication.Message;
import main.java.logger.SimpleLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Patryk on 30.03.2018.
 */
public class ChatServer implements Runnable {

    // Server port
    private final int SERVER_PORT = 2222;

    // The server socket for broadcast.
    private ServerSocket serverSocket = null;

    private Map<String, ClientService> connectedClients;

    private SimpleLogger logger;

    public ChatServer() {
        connectedClients = new HashMap<>();
        logger = SimpleLogger.getInstance();
        run();
    }

    @Override
    public void run() {

    }

    public synchronized void registerClient(String clientID, ClientService clientService) {
        connectedClients.put(clientID, clientService);
    }

    public synchronized void sendMessageToClient(String clientID, Message message) {
        connectedClients.get(clientID).writeMessage(message);
    }

    private void initializeServer() {
        logger.logMessage("Starting server on port: " + SERVER_PORT);
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            logger.logMessage("Server started successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeClients() {
        //readClientsFromFile
    }

    public static void main(String args[]) {

        //new ChatServer();

    }
}
