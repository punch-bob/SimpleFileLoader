package com.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.common.Message;

public class ServerListener extends Thread{
    private ObjectInputStream inputStream;
    private Socket socket;

    public ServerListener(Socket socket) {
        this.socket = socket;
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            Message message = null;
            try {
                message = (Message)inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                message = null;
            }

            if (message != null) {
                String str = new String(message.getData());
                System.out.println(str);
                if (str.contains("completely")) {
                    break;
                }
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
