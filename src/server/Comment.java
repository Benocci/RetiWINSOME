package server;

import java.util.Date;

public class Comment {
    private final String author;
    private final String content;
    private final Date date;

    public Comment(String author, String content){
        this.author = author;
        this.content = content;
        this.date = new Date();
    }

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
