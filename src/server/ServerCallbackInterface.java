package server;

import client.NotifyEventInterface;

import java.rmi.*;

public interface ServerCallbackInterface extends Remote {
    public void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException;

    public void unregisterForCallback(String username) throws RemoteException;
}
