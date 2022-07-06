package client;

import java.rmi.*;
import java.rmi.server.*;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: implementa la notifica del cambiamento nei follower del client
 */
public class NotifyEvent extends RemoteObject implements NotifyEventInterface{

    @Override
    public void notifyEvent(int value, String username) throws RemoteException {
        if(value == 1){ // inzia a seguirti
            System.out.println("\n < "+ username + " ha iniziato a seguirti!");
            ClientMainWINSOME.followers.add(username);
        }
        else if(value == -1){ // smette di seguirti
            System.out.println("\n < "+ username + " ha smesso di seguirti!");
            ClientMainWINSOME.followers.remove(username);
        }
        System.out.print("> ");
    }
}
