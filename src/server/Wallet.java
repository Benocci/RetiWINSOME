package server;

import server.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Wallet {
    private final String username;
    private final ConcurrentLinkedQueue<Transaction> transactions;

    public Wallet(String username){
        this.username = username;
        this.transactions = new ConcurrentLinkedQueue<>();
    }

    public String getUsername() {
        return username;
    }

    public ConcurrentLinkedQueue<Transaction> getTransactions() {
        return transactions;
    }

    public int getWalletAmount(){
        int to_return = 0;
        for (Transaction t: transactions) {
            to_return += t.getValue();
        }

        return to_return;
    }

    public float getWalletinBitcoin(){
        float random_value = 0;
        try{
            URL url = new URL("https://www.random.org/decimal-fractions/?num=1&dec=20&col=1&format=plain&rnd=new");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if(httpURLConnection.getResponseCode() >= 300){
                System.out.println("ERROR: random.org unreachable");
            }
            else{
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String readed = bufferedReader.readLine();
                random_value = Float.parseFloat(readed);
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }

        return random_value*getWalletAmount();
    }

    public Boolean addTransaction(int transaction_value){
        return transactions.add(new Transaction(transaction_value));
    }
}
