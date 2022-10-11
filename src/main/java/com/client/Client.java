package com.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.common.Message;

public class Client implements Runnable {
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String filename;
    private File file;
    private InputStream fileInput;
    private int partSize = 1024;
    private Socket socket;

    public Client(int serverPort, String hostname, String filename) {
        this.filename = filename;
        try {
            this.socket = new Socket(hostname, serverPort);
            this.socket.setSoTimeout(1000);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.file = new File(filename);
            this.fileInput = new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public int getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0; 

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };
        
        fis.close();
        
        byte[] bytes = digest.digest();        
        return ByteBuffer.wrap(bytes).getInt();
    }

    public void sendInitMessage() {
        MessageDigest md5Digest = null;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        int lastSlashId = this.filename.lastIndexOf("/");
        String newFilename = this.filename.substring(lastSlashId);
        try {
            sendMessage(newFilename.getBytes(), this.getFileChecksum(md5Digest, this.file), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        
        if (fileInput != null) {
            try {
                fileInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        Message message = null;
        try {
             message = (Message)inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            message = null;
            e.printStackTrace();
        }
        if (message != null) {
            String str = new String(message.getData());
            System.out.println(str);
        }
        closeConnection(); 
    }
}