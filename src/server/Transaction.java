package server;

import java.util.Date;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta una transazione
 */
public class Transaction {
    private final double value;
    private final Date date;


    public Transaction(double value){
        this.value = value;
        this.date = new Date();
    }

    public double getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }
}
