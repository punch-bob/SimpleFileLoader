package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private ServerSocket server;
    private SessionManager manager;

    public Server(int port) {
        manager = new SessionManager();
        try {
            server = new ServerSocket(port, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClient(ClientListener client) {
        manager.addClient(client);
    }

    public void removeClient(ClientListener client) {
        manager.removeClient(client);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = server.accept();
                addClient(new ClientListener(socket, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
