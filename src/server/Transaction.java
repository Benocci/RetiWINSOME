package server;

import java.util.Date;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta una transazione
 */
public class Transaction {
    private final int value;
    private final Date date;


    public Transaction(int value){
        this.value = value;
        this.date = new Date();
    }

    public int getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }
}
