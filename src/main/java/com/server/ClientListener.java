package com.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.common.Message;

public class ClientListener extends Thread
{
    private Server server;
    private Socket socket;
    private ObjectInputStream inSocketStream;
    private ObjectOutputStream outSocketStream;
    private FileOutputStream writer ;
    private String filename;
    private int fileSize;
    private boolean lastPart = false;
    private int timeout = 3000;


    public ClientListener(Socket socket, Server server) throws IOException
    {
        this.server = server;
        this.socket = socket;
        outSocketStream  = new ObjectOutputStream(socket.getOutputStream());
        inSocketStream = new ObjectInputStream(socket.getInputStream());
        socket.setSoTimeout(1000);
        this.start();
    }

    public void closeConnection() {
        server.removeClient(this);
        try {
            if (!socket.isClosed()) {
                socket.close();
                inSocketStream.close();
                outSocketStream.close();
                writer.close();
            }
        }
        catch (IOException e) {}
    }

    public int getTimeout() {
        return timeout;
    }

    private void setFileParams() {
        Message clientInitMessage = null;
        while (clientInitMessage == null) {
            try {
                clientInitMessage = (Message)inSocketStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                clientInitMessage = null;
            }
        }
        this.filename = new String(clientInitMessage.getData(), StandardCharsets.UTF_8);
        this.fileSize = clientInitMessage.getSize();
    }

    public int readFilePart() {
        Message message = null;
        int readedBytes = 0;
        try {
            message = (Message)inSocketStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            message = null;
        }

        if (message != null) {
            this.lastPart = message.isLastPart();
            try {
                writer.write(message.getData(), 0, message.getSize());
                writer.flush();
                readedBytes = message.getSize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return readedBytes;
    }

    public void sendMessage(String data) {
        Message message = new Message(data.getBytes(), data.getBytes().length, true);
        try {
            outSocketStream.writeObject(message);
            outSocketStream.flush();
        } catch (IOException e) {}
    }

    @Override
    public void run() {
        setFileParams();
        try {
            writer = new FileOutputStream(Path.of("").toAbsolutePath().toString() + "/src/main/java/com/server/uploads/" + this.filename, false);
        } catch(IOException e) { 
            e.printStackTrace();
        } 
        long start = System.currentTimeMillis();
        long prevTimeUpd = System.currentTimeMillis();
        long currBytes = 0;
        long readedBytes = 0;
        while (!this.lastPart) {
            long tmp = readFilePart();
            currBytes += tmp;
            readedBytes += tmp;
            long currTime = System.currentTimeMillis() - prevTimeUpd;
            if (currTime >= timeout) {
                System.out.println("Current speed: " + (currBytes / currTime * 1000 / 1024) + " Kbps\nAverage speed: " + (readedBytes / (System.currentTimeMillis() - start) * 1000 / 1024) + " Kbps\n");
                System.out.println("============================================");
                prevTimeUpd = System.currentTimeMillis();
                currBytes = 0;
            }
        } 
        if (readedBytes == fileSize) {
            sendMessage("The file has been delivered completely!\n");
        } else {
            sendMessage("The file has been delivered not completely!\n");
        }
        System.out.println("Average speed: " + (readedBytes / (System.currentTimeMillis() - start) * 1000 / 1024) + " Kbps");
        closeConnection();
    }
}