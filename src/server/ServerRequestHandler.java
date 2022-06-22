package server;

import exception.*;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe runnable che viene usata dalla threadpool per elaborare le richieste del client e inviare la risposta
 */
public class ServerRequestHandler implements Runnable {
    SocialNetwork social;
    String channel;
    SocketChannel socketChannel;
    Selector selector;
    String request;
    ServerCallback callback;

    public ServerRequestHandler(SocialNetwork social, String channel, SocketChannel socketChannel, Selector selector, String request, ServerCallback callback){
        this.channel = channel;
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.social = social;
        this.request = request;
        this.callback = callback;
    }


    /*
     * EFFECTS: chima requestHandler e invia la stringa di ritorno al client
     * THROWS: IOException se occorrono errori nella write della risposta sul socketchannel
     */
    @Override
    public void run() {
        System.out.println("Messaggio ricevuto dal client: " + channel + ": " + request);

        String res = requestHandler(request, channel, social, callback);

        ByteBuffer to_send = ByteBuffer.allocate(Integer.BYTES + res.length());
        to_send.putInt(res.length());
        to_send.put(res.getBytes());
        to_send.flip();

        try {
            socketChannel.write(to_send);
        } catch (IOException e) {
            e.printStackTrace();
        }
        to_send.clear();
    }


    /*
     * REQUIRES: request != null ∧ channel != null ∧ social != null
     * MODIFIES: this in caso di richieste di modifica
     * EFFECTS: elabora la richiesta e ritorna una stringa di risposta in base al successo o meno dell'operazione
     * THROWS:  UserNotExistException se un utente non è presente nel socialnewtok
     *          PostNotExistException se un post non esiste
     *          VoteNotValidException se il voto in una operazione di rate non è -1/+1
     *          SameUserException se un utente prova a votare un suo post
     *          NoAuthorizationException se non ci sono le autorizzazioni per effettuare un operazione
     */
    private static String requestHandler(String request, String channel, SocialNetwork social, ServerCallback callback){
        //parsing della richiesta:
        ArrayList<String> line_parsed = new ArrayList<>();
        Collections.addAll(line_parsed, request.split(" "));
        String option = line_parsed.remove(0);
        String res = "";

        switch (option) {
            case "login": { // richiesta di login
                if (line_parsed.size() != 2) { // controllo sul numero di argomenti
                    System.out.println("Errore!");
                    res = "numero argomenti";
                    break;
                }

                String username = line_parsed.get(0);
                String password = line_parsed.get(1);

                if (ServerMainWINSOME.loggedUsers.contains(username)) {//controllo se l'utente è già loggato
                    System.out.println("Utente già loggato");
                    res = "utente già loggato";
                    break;
                }

                User user = social.getUser(username);

                if (user == null) {
                    System.out.println("Utente non esiste");
                    res = "utente non esiste";
                } else {                                        // se l'utente esiste
                    if (user.getPassword().equals(password)) {  // e la sua password corrisponde loggo
                        ServerMainWINSOME.loggedUsers.put(channel, username);
                        res = "ok";
                    } else {
                        res = "password errata";
                    }
                }

                break;
            }
            case "logout": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    res = "utente non loggato";
                    break;
                }

                ServerMainWINSOME.loggedUsers.remove(channel);
                res = "ok";
                break;
            }
            case "list": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                ArrayList<String> list = null;
                switch (line_parsed.get(0)) {
                    case "followers": {
                        list = social.getFollowers(username);
                        break;
                    }
                    case "following": {
                        list = social.getFollowing(username);
                        break;
                    }
                    case "users": {
                        list = social.listUsers(social.getUser(username));
                        break;
                    }
                }
                System.out.println(list);
                res = "ok";

                break;
            }
            case "follow": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.followUser(username, line_parsed.get(0));
                    System.out.println(username + " segue " + line_parsed.get(0));
                    callback.notifyClient(1,username, line_parsed.get(0));
                    res = "ok";
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                    res = "utente non esiste";
                }
                catch (RemoteException ex){
                    ex.printStackTrace();
                    res = "ERRORE NELLA CALLBACK";
                }

                break;
            }
            case "unfollow": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.unfollowUser(username, line_parsed.get(0));
                    System.out.println(username + " non segue più " + line_parsed.get(0));
                    callback.notifyClient(-1,username, line_parsed.get(0));
                    res = "ok";
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                }
                catch (RemoteException ex){
                    ex.printStackTrace();
                    res = "ERRORE NELLA CALLBACK";
                }

                break;
            }
            case "blog": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                ArrayList<Post> postView = null;
                try {
                    postView = social.viewBlog(username);
                    System.out.println(username + " post view:");
                    for (Post p : postView) {
                        System.out.println(" ID post: " + p.getId() + ", autore: " + p.getAuthor() + ", titolo: " + p.getTitle());
                    }
                    res = "ok";
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                    res = "utente non esiste";
                }

                break;
            }
            case "post": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                if(request.chars().filter(ch -> ch == '"').count() != 4){
                    res = "comando non corretto";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try{
                    request = request.substring(request.indexOf("\"")+1);
                    String title = request.substring(0, request.indexOf("\""));
                    request = request.substring(request.indexOf("\"")+1);
                    request = request.substring(request.indexOf("\"")+1);
                    String content = request.substring(0,request.indexOf("\""));

                    try {
                        social.addPost(username, title, content);
                        res = "ok";
                    } catch (UserNotExistException e) {
                        e.printStackTrace();
                        res = "utente non esiste";
                    }
                }
                catch (IndexOutOfBoundsException e){
                    res = "comando non corretto";
                }
            }
            case "show": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                switch (line_parsed.get(0)) {
                    case "feed": {

                        ArrayList<String> followerList = social.getFollowers(username);
                        try {
                            for (String author : followerList) {
                                ArrayList<Post> authorPostView = social.viewBlog(author);
                                for (Post p : authorPostView) {
                                    System.out.println(" ID post: " + p.getId() + ", autore: " + p.getAuthor() + ", titolo: " + p.getTitle());
                                }

                            }
                            res = "ok";
                        } catch (UserNotExistException e) {
                            e.printStackTrace();
                            res = "utente non esiste";
                        }

                        break;
                    }
                    case "post": {
                        if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                            System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                            res = "utente non loggato";
                            break;
                        }

                        try {
                            Post post = social.getPost(Integer.parseInt(line_parsed.get(1)));
                            System.out.println("-ID post: " + post.getId() + ", autore: " + post.getAuthor() + ", titolo: " + post.getTitle());
                            System.out.println(" Contenuto: " + post.getContent());
                            System.out.println(" Voto: " + post.getVote() + ", numero di voti: " + post.getVotes().size());
                            for (Comment c: post.getComments()) {
                                System.out.println(" Commento di " + c.getAuthor() + " dice: " + c.getContent());
                            }
                            res = "ok";
                        } catch (PostNotExistException e) {
                            e.printStackTrace();
                            res = "utente non esiste";
                        }

                        break;
                    }
                }

                break;
            }
            case "delete": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    Post post = social.getPost(Integer.parseInt(line_parsed.get(0)));
                    if (username.equals(post.getAuthor())) {
                        social.removePost(Integer.parseInt(line_parsed.get(0)), username);
                    } else {
                        System.out.println("Devi essere l'autore per eliminare un post!");
                    }
                } catch (PostNotExistException | NoAuthorizationException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "rewin": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.rewinPost(Integer.parseInt(line_parsed.get(0)), username);
                    res = "ok";
                } catch (PostNotExistException e) {
                    e.printStackTrace();
                    res = "post non esiste";
                } catch (SameUserException e) {
                    e.printStackTrace();
                    res = "voto ad un tuo post";
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                    res = "utente non esiste";
                }

                break;
            }
            case "rate": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                try {
                    social.ratePost(Integer.parseInt(line_parsed.get(0)), username, Integer.parseInt(line_parsed.get(1)));
                    res = "ok";
                } catch (UserNotExistException e){
                    e.printStackTrace();
                    res = "utente non esiste";
                } catch (SameUserException e) {
                    e.printStackTrace();
                    res = "voto ad un tuo post";
                } catch (VoteNotValidException e){
                    e.printStackTrace();
                    res = "voto non valido";
                }catch(PostNotExistException e) {
                    e.printStackTrace();
                    res = "post non esiste";
                }
                break;
            }
            case "comment": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                int id_post = Integer.parseInt(line_parsed.remove(0));
                request = request.substring(request.indexOf("\"")+1);
                String comment_content = request.substring(0, request.indexOf("\""));
                try {
                    social.commentPost(id_post, username, comment_content);
                    res = "ok";
                } catch (PostNotExistException e) {
                    e.printStackTrace();
                    res = "post non esiste";
                } catch (UserNotExistException e){
                    e.printStackTrace();
                    res = "utente non esiste";
                } catch (SameUserException e) {
                    e.printStackTrace();
                    res = "voto ad un tuo post";
                }
                break;
            }
            case "wallet": {
                if (!ServerMainWINSOME.loggedUsers.containsKey(channel)) {
                    System.out.println("Utente non loggato, impossibile svolgere l'operazione.");
                    res = "utente non loggato";
                    break;
                }

                String username = ServerMainWINSOME.loggedUsers.get(channel);
                Wallet wallet = null;
                try {
                    wallet = social.getWallet(username);
                    res = "ok";
                } catch (UserNotExistException e) {
                    e.printStackTrace();
                    res = "utente non esiste";
                    break;
                }

                if (line_parsed.get(0).equals("btc")) {
                    System.out.println("Valore del wallet in bitcoin: " + wallet.getWalletinBitcoin());
                } else {
                    System.out.println("Walore del wallet: " + wallet.getWalletAmount());
                }

                break;
            }
            default: {
                System.out.println("Messaggio dal client non riconosciuto!");
                res = "messaggio non riconosciuto";
            }

        }

        return res;
    }
}
