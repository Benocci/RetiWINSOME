package server;

import java.util.Date;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta un commento ad un post
 */
public class Comment {
    private final String author;
    private final String content;
    private final Date date;

    public Comment(String author, String content){
        this.author = author;
        this.content = content;
        this.date = new Date();
    }

    //metodi get dei campi del commento
    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
