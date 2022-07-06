package server;

import client.NotifyEventInterface;

import java.rmi.*;
import java.util.ArrayList;

public interface ServerCallbackInterface extends Remote {
    public void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException;

    public void unregisterForCallback(String username) throws RemoteException;

    public ArrayList<String> getFollowersList(String username) throws RemoteException;
}
