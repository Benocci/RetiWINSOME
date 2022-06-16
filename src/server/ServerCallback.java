package server;


import client.NotifyEventInterface;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW:
 */
public class ServerCallback extends RemoteServer implements ServerCallbackInterface{
    private HashMap<String, NotifyEventInterface> clients= new HashMap();

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    @Override
    public void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException {
        clients.putIfAbsent(username, ClientInterface);
    }

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    @Override
    public void unregisterForCallback(String username) throws RemoteException {
        clients.remove(username);
    }

    public synchronized void notifyClient(int value, String username, String follower) throws RemoteException {
        System.out.println("Notifica " + value + " al client sull'utente " + username);

        clients.get(username).notifyEvent(value, follower);
    }
}
