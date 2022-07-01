package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta il portafoglio di un utente
 */
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

    /*
     * EFFECTS: return il valore totale del portafoglio
     */
    public int getWalletAmount(){
        int to_return = 0;
        for (Transaction t: transactions) {
            to_return += t.getValue();
        }

        return to_return;
    }

    /*
     * EFFECTS: return il valore totale del portafoglio in bitcoin
     * THROWS: IOException se occorrono errori I/O
     */
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

    /*
     * REQUIRES:
     * MODIFIES: this.transactions
     * EFFECTS: aggiunge una nuova transazione
     */
    public Boolean addTransaction(double transaction_value){
        return transactions.add(new Transaction(transaction_value));
    }
}
