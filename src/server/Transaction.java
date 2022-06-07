package server;

import java.util.Date;

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
