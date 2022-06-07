package server;

import java.util.Date;
import java.util.Set;

public class User {
    private final String username;
    private final String password;
    private final Set<String> tags;
    private final Wallet wallet;
    private final Date date;


    public User(String username, String password, Set<String> tags){
        this.username = username;
        this.password = password;
        this.tags = tags;
        this.wallet = new Wallet(username);
        this.date = new Date();
    }

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

    public Set<String> getTags() {
        return tags;
    }
}