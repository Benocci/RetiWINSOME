package client;

import java.rmi.*;
import java.rmi.server.*;

public class NotifyEvent extends RemoteObject implements NotifyEventInterface{

    @Override
    public void notifyEvent(int value, String username) throws RemoteException {
        if(value == 1){ // inzia a seguirti
            System.out.println(username + "ha iniziato a seguirti!");
            ClientMainWINSOME.follower.add(username);
        }
        else if(value == -1){ // smette di seguirti
            System.out.println(username + "ha smesso di seguirti!");
            ClientMainWINSOME.follower.remove(username);
        }
    }
}