package com.server;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Wrong number of arguments!\n Usage: Main <port>\n");
            return;
        }
        new Server(Integer.parseInt(args[0])).run();
    }
}