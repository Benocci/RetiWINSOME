package server;

import java.util.ArrayList;
import java.util.Date;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta un utente all'interno di WINSOME
 */
public class User {
    private final String username;
    private final String password;
    private final ArrayList<String> tags;
    private final Wallet wallet;
    private final Date date;


    public User(String username, String password, ArrayList<String> tags){
        this.username = username;
        this.password = password;
        this.tags = tags;
        this.wallet = new Wallet(username);
        this.date = new Date();
    }

    //metodi get dei campi dell'utente

    public Date getDate() {
        return date;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public ArrayList<String> getTags() {
        return tags;
    }
}
