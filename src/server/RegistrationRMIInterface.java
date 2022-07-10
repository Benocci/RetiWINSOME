package server;

import java.rmi.*;
import java.util.ArrayList;

public interface RegistrationRMIInterface extends Remote{

    public boolean register(String username, String password, ArrayList<String> tags) throws RemoteException;

}
