package server;

import exception.PostNotExistException;
import exception.SameUserException;
import exception.UserNotExistException;
import exception.VoteNotValidException;
import shared.ConfigWINSOME;


import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;

public class ServerRequestHandler implements Runnable {
    ConfigWINSOME config;
    SocialNetwork social;
    String channel;
    String request;

    public ServerRequestHandler(ConfigWINSOME config,SocialNetwork social, String channel, String request){
        this.config = config;
        this.channel = channel;
        this.social = social;
        this.request = request;
    }


    @Override
    public void run() {

        System.out.println("Messaggio ricevuto dal client: " + channel + ": " + request);

        ArrayList<String> line_parsed = new ArrayList<>();
        Collections.addAll(line_parsed, request.split(" "));
        String option = line_parsed.remove(0);

        switch (option){
            case "login":{
                if(line_parsed.size() != 2){
                    System.out.println("Errore!");
                }
                String username = line_parsed.get(0);
                String password = line_parsed.get(1);

                if(ServerMainWINSOME.loggedUsers.contains(username)){
                    System.out.println("Utente già loggato");
                    return;
                }

                User user = social.getUser(username);

                if(user == null){
                    System.out.println("Utente non esiste");
                }
                else{
                    if(user.getPassword().equals(password)){
                        ServerMainWINSOME.loggedUsers.put(channel ,username);
                        System.out.println("Utente loggato!");
                    }
                    else{
                        System.out.println("Password errata!");
                    }
                }

                break;
            }
            case "logout":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    return;
                }

                ServerMainWINSOME.loggedUsers.remove(channel);

                break;
            }
            case "list":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    return;
                }
                String username = ServerMainWINSOME.loggedUsers.get(channel);
                ArrayList<String> list = null;
                switch (line_parsed.get(0)){
                    case "followers":{
                        list = social.getFollowers(username);
                        break;
                    }
                    case "following":{
                        list = social.getFollowing(username);
                        break;
                    }
                    case "users":{
                        list = social.listUsers(social.getUser(username));
                        break;
                    }
                }
                System.out.println(list);

                break;
            }
            case "follow":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.followUser(username, line_parsed.get(0));
                    System.out.println(username + " segue " + line_parsed.get(0));
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "unfollow":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.unfollowUser(username, line_parsed.get(0));
                    System.out.println(username + " non segue più " + line_parsed.get(0));
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "blog":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                ArrayList<Post> postView = null;
                try {
                    postView = social.viewBlog(username);
                    System.out.println(username + " post view:");
                    for (Post p: postView) {
                        System.out.println( " ID post: " + p.getId() + ", autore: " + p.getAuthor() + ", titolo: " + p.getTitle());
                    }
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "post":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.addPost(username, line_parsed.get(0), line_parsed.get(1));
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                }

            }
            case "show":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }
                String username = ServerMainWINSOME.loggedUsers.get(channel);
                switch (line_parsed.get(0)){
                    case "feed":{

                        ArrayList<String> followerList = social.getFollowers(username);
                        try {
                            for (String author: followerList) {
                                ArrayList<Post> authorPostView = social.viewBlog(author);
                                for (Post p: authorPostView) {
                                    System.out.println( " ID post: " + p.getId() + ", autore: " + p.getAuthor() + ", titolo: " + p.getTitle());
                                }

                            }
                        } catch (UserNotExistException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                    case "post":{

                        try {
                            Post post = social.getPost(Integer.parseInt(line_parsed.get(0)));
                            System.out.println(" ID post: " + post.getId() + ", autore: " + post.getAuthor() + ", titolo: " + post.getTitle());
                            System.out.println("altre info del post");
                        } catch (PostNotExistException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                }

                break;
            }
            case "delete":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    Post post = social.getPost(Integer.parseInt(line_parsed.get(0)));
                    if(username.equals(post.getAuthor())){
                        social.removePost(Integer.parseInt(line_parsed.get(0)));
                    }
                    else{
                        System.out.println("Devi essere l'autore per eliminare un post!");
                    }
                } catch (PostNotExistException e) {
                        e.printStackTrace();
                }

                break;
            }
            case "rewin":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.rewinPost(Integer.parseInt(line_parsed.get(0)), username);
                } catch (PostNotExistException | SameUserException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "rate":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.ratePost(Integer.parseInt(line_parsed.get(0)), username, Integer.parseInt(line_parsed.get(1)));
                } catch (UserNotExistException | SameUserException | VoteNotValidException | PostNotExistException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "comment":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.commentPost(Integer.parseInt(line_parsed.get(0)), username, line_parsed.get(1));
                } catch (SameUserException | UserNotExistException | PostNotExistException e) {
                    e.printStackTrace();
                }
            }
            case "wallet":{
                if(!ServerMainWINSOME.loggedUsers.containsKey(channel)){
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                Wallet wallet = null;
                try {
                    wallet = social.getWallet(username);
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                }

                if(line_parsed.get(0).equals("btc")){
                    System.out.println("Valore del wallet in bitcoin: " + wallet.getWalletinBitcoin());
                }
                else {
                    System.out.println("Walore del wallet: " + wallet.getWalletAmount());
                }

                break;
            }
            default:{
                System.out.println("Messaggio dal client non riconosciuto!");
            }

            String res = "ALL GOOD";

            ByteBuffer to_send = ByteBuffer.allocate(Integer.BYTES + res.length());
            to_send.putInt(res.length());
            to_send.put(res.getBytes());
            to_send.flip();

            //capisci come fare la register sul selector per mandare
            //socketchannel.register(selector, SelectionKey.OP_WRITE, to_send);
        }
    }
}
