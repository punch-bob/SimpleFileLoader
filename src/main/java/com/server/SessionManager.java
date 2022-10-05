package com.server;

import java.util.HashSet;
import java.util.Set;

public class SessionManager 
{
    private Set<ClientListener> clients = new HashSet<>();

    public synchronized void addClient(ClientListener clientListener)
    {
        clients.add(clientListener);
    }

    public synchronized void removeClient(ClientListener clientListener)
    {
        clients.remove(clientListener);
    }

    public synchronized Set<ClientListener> getClients()
    {
        return clients;
    }
}