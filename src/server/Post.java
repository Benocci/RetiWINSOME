package server;

import exception.SameUserException;
import server.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Post {
    private final int id;
    private final String author;
    private final String title;
    private final String content;
    private final Date date;
    private final ConcurrentHashMap<String, Integer> votes;
    private final ConcurrentLinkedQueue<String> rewinUsers;
    private final ArrayList<Comment> comments;

    public Post(int id, String author, String title, String content){
        this.id = id;
        this.author = author;
        this.title = title;
        this.content = content;
        this.date = new Date();
        this.votes = new ConcurrentHashMap<>();
        this.rewinUsers = new ConcurrentLinkedQueue<>();
        this.comments = new ArrayList<>();
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

    public int getId() {
        return id;
    }

    public int getVote() {
        int to_return = 0;

        for (Integer v: votes.values()) {
            to_return += v;
        }
        return to_return;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void addVote(String username, int value) throws SameUserException {
        if(author.equals(username)){
            throw new SameUserException();
        }

        votes.putIfAbsent(username, value);
    }

    public void addComment(String username, String content) throws SameUserException{
        if(author.equals(username)){
            throw new SameUserException();
        }

        comments.add(new Comment(username, content));
    }

    public void addRewin(String username) throws SameUserException {
        if(author.equals(username)){
            throw new SameUserException();
        }

        rewinUsers.add(username);
    }
}