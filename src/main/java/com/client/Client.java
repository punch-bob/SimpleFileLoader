package com.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


import com.common.Message;

public class Client implements Runnable {
    private ObjectOutputStream outputStream;
    private String filename;
    private File file;
    private InputStream fileInput;
    private int partSize = 1024;
    private Socket socket;
    private ServerListener serverListener;

    public Client(int serverPort, String hostname, String filename) {
        this.filename = filename;
        try {
            this.socket = new Socket(hostname, serverPort);
            this.socket.setSoTimeout(1000);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.serverListener = new ServerListener(socket);
            this.file = new File(filename);
            this.fileInput = new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverListener.start();
    }

    public void sendMessage(byte[] data, int size, boolean isLastPart) {
        Message message = new Message(data, size, isLastPart);
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInitMessage() {
        int lastSlashId = this.filename.lastIndexOf("/");
        String newFilename = this.filename.substring(lastSlashId);
        sendMessage(newFilename.getBytes(), (int)file.length(), false);
    }

    public void closeConnection() {
        if (fileInput != null) {
            try {
                fileInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        sendInitMessage();
        byte[] data = new byte[partSize];
        int readedBytes = 0;
        try {
            readedBytes = fileInput.read(data, 0, partSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        while (readedBytes != -1) {
            boolean isLast = false;      
            if (readedBytes < partSize) {
                isLast = true;
            }
            sendMessage(data, readedBytes, isLast);
            data = new byte[partSize];     
            try {
                readedBytes = fileInput.read(data, 0, partSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closeConnection(); 
    }
}