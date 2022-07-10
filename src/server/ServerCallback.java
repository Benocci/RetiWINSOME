package server;


import client.NotifyEventInterface;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW:
 */
public class ServerCallback extends RemoteServer implements ServerCallbackInterface{
    private ConcurrentHashMap<String, NotifyEventInterface> clients= new ConcurrentHashMap<>();

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    @Override
    public synchronized void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException {
        clients.putIfAbsent(username, ClientInterface);
    }

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    @Override
    public synchronized void unregisterForCallback(String username) throws RemoteException {
        clients.remove(username);
    }

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    public synchronized void notifyClient(int value, String username, String follower) throws RemoteException {
        if(clients.containsKey(follower)){
            clients.get(follower).notifyEvent(value, username);
        }
    }

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    @Override
    public ArrayList<String> getFollowersList(String username) throws RemoteException {
        SocialNetwork socialNetwork = ServerMainWINSOME.socialNetwork;

        return socialNetwork.getFollowers(username);

    }
}
