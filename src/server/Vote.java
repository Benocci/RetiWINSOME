package server;

import java.util.Date;

public class Vote {
    private final String author;
    private final Date date;
    private final int rate;

    public Vote(String author, Date date, int rate) {
        this.author = author;
        this.date = date;
        this.rate = rate;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public int getRate() {
        return rate;
    }
}
