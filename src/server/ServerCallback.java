package server;


import client.NotifyEventInterface;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe remota che implementa ServerCallbackInterface con metodi chiamati dal client per registrarsi alle notifiche di follow/unfollow
 */
public class ServerCallback extends RemoteServer implements ServerCallbackInterface{
    //mappa che contiene i client registrati e pronti a ricevere notifiche in caso di follow e unfollow
    private ConcurrentHashMap<String, NotifyEventInterface> clients= new ConcurrentHashMap<>();

    /*
     * REQUIRES: username != null && ClientInterface != null
     * MODIFIES: clients
     * EFFECTS: aggiunge alla mappa clients username come key e ClientInterface come value
     * THROWS: RemoteException
     */
    @Override
    public void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException {
        clients.putIfAbsent(username, ClientInterface);
    }

    /*
     * REQUIRES: username != null
     * MODIFIES: clients
     * EFFECTS: rimuove la coppia String NotifyEventInterface identificata da username
     * THROWS: RemoteException
     */
    @Override
    public void unregisterForCallback(String username) throws RemoteException {
        clients.remove(username);
    }

    /*
     * REQUIRES: (value == 1 || value == -1) && username != null && follower != null
     * EFFECTS: chiama il metodo notifyEvent dell'interfaccia NotifyEventInterface associato al client follower
     * THROWS: RemoteException
     */
    public void notifyClient(int value, String username, String follower) throws RemoteException {
        if(clients.containsKey(follower)){
            clients.get(follower).notifyEvent(value, username);
        }
    }

    /*
     * REQUIRES: username != null
     * EFFECTS: ritorna la lista dei followers di username al client
     * THROWS: RemoteException
     * RETURN: ArrayList di stringhe contenenti i followers
     */
    @Override
    public ArrayList<String> getFollowersList(String username) throws RemoteException {
        SocialNetwork socialNetwork = ServerMainWINSOME.socialNetwork;

        return socialNetwork.getFollowers(username);

    }
}
