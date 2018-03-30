package main.java;

import main.java.communication.Message;
import main.java.communication.Protocol;

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


    public ClientService(Socket clientSocket, ChatServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {

        while (true) {
            try {
                Message message = (Message) inputStream.readObject();

                if (message.getMessageType().equals(Protocol.REGISTER)) {

                } else if(message.getMessageType().equals(Protocol.COMMUNICATION)) {

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

    private void initializeStreans() {
        try {
            this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
            this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
