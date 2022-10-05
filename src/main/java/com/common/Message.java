package com.common;

import java.io.Serializable;

public class Message implements Serializable {
    private byte[] data;
    private int size;
    private boolean lastPart;

    public Message(byte[] message, int size, boolean lastPart) {
        this.data = message;
        this.size = size;
        this.lastPart = lastPart;
    } 

    public byte[] getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    public boolean isLastPart() {
        return lastPart;
    }
}
