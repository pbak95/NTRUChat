import logger.SimpleLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Patryk on 31.03.2018.
 */
public class ChatClient implements Runnable {

    // Server port
    private final int SERVER_PORT = 2222;
    private final String HOST = "localhost";


    private Socket clientSocket;
    /** object to send */
    private ObjectOutputStream outputStream;
    /** object to read */
    private ObjectInputStream inputStream;

    private static Scanner inputLine;

    private SimpleLogger logger;

    public ChatClient() {
        logger = SimpleLogger.getInstance();
        initializeClient();
        run();
    }

    @Override
    public void run() {

    }

    private void initializeClient() {
        logger.logMessage("Starting client");
        try {
            clientSocket = new Socket(HOST, SERVER_PORT);
            initializeStreams();
            logger.logMessage("Client started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeStreams() throws IOException {
        this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
        this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      }

    public static void consoleHandler() {

    }
}
