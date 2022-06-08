package client;

import java.rmi.*;

public interface NotifyEventInterface extends Remote {

    public void notifyEvent(int value, String username) throws RemoteException;
}
