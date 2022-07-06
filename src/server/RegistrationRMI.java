package server;

import java.rmi.RemoteException;
import java.util.ArrayList;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW:
 */
public class RegistrationRMI implements RegistrationRMIInterface{

    @Override
    public void register(String username, String password, ArrayList<String> tags) throws RemoteException {
        SocialNetwork socialNetwork = ServerMainWINSOME.socialNetwork;

        String user = username.toLowerCase();

        if(socialNetwork.userExist(user)){
            System.out.println("Utente già presente!");
            return;
        }

        socialNetwork.addUser(new User(user, password, tags));

        System.out.println("Utente " + user + " aggiunto");
    }

}
