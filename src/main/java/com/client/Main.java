package com.client;

public class Main {
    public static void main (String [] args) {
        if  (args.length != 3) {
            System.out.println("Wrong number of arguments!\n Usage: Main <DNS(IP)> <server-port> <filename>\n");
            return;
        }
        Client client = new Client(Integer.parseInt(args[1]), args[0], args[2]);
        new Thread(client).start();
    }
}
