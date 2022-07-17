package server;

import java.rmi.RemoteException;
import java.util.ArrayList;


/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe remota che implementa RegistrationRMIInterface con overrid del metodo register
 */
public class RegistrationRMI implements RegistrationRMIInterface{

    /*
     * REQUIRES: username != null && password != null && tags != null && tags.size() > 0
     * MODIFIES: socialNetwork
     * EFFECTS: aggiunge un nuovo utente con i campi dati
     * THROWS: RemoteException
     */
    @Override
    public boolean register(String username, String password, ArrayList<String> tags) throws RemoteException {
        SocialNetwork socialNetwork = ServerMainWINSOME.socialNetwork;

        String user = username.toLowerCase();

        return socialNetwork.addUser(new User(user, password, tags));
    }

}
