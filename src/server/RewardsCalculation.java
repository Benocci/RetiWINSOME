package server;

import exception.UserNotExistException;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW:
 */
public class RewardsCalculation implements Runnable{
    ConfigServerWINSOME config;
    SocialNetwork social;
    int rewards_period = 10000;
    Date last_calculation;

    private Boolean continueLoop = true;

    public RewardsCalculation(ConfigServerWINSOME config, SocialNetwork social){
        this.config = config;
        this.social = social;
        this.last_calculation = new Date();
    }

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    @Override
    public void run() {
        try ( DatagramSocket datagramSocketServer = new DatagramSocket(6800) ){
            DatagramPacket datagramPacket;

            ConcurrentHashMap<Integer, Post> postMap;

            while(continueLoop){
                postMap = social.getPostMap();
                double reward, total_reward = 0;

                try{
                    Thread.sleep(rewards_period);
                }
                catch (InterruptedException ignore){
                    ;
                }

                if(postMap.size() != 0){

                    for(Post p: postMap.values()) {
                        if(p.hadChange(last_calculation)){
                            reward = profitCalculation(p);
                            total_reward += reward;
                        }
                    }

                    if(total_reward != 0){
                        String to_send = "Reward totale:" + total_reward;
                        //System.out.println("DEBUG (invio reward): " + to_send);

                        InetAddress clientAddress = InetAddress.getByName(config.getMulticast_address());
                        //invio la lunghezza della stringa contenente il reward
                        ByteBuffer lenghtBuffer = ByteBuffer.allocate(Integer.BYTES);
                        lenghtBuffer.putInt(to_send.getBytes().length);
                        datagramPacket = new DatagramPacket(lenghtBuffer.array(), lenghtBuffer.limit(), clientAddress ,config.getMulticast_port());
                        datagramSocketServer.send(datagramPacket);

                        //invio la stringa contenente il reward
                        ByteBuffer to_sendBuffer = ByteBuffer.allocate(to_send.getBytes().length);
                        to_sendBuffer.put(to_send.getBytes());
                        datagramPacket = new DatagramPacket(to_sendBuffer.array(), to_sendBuffer.limit(), clientAddress ,config.getMulticast_port());
                        datagramSocketServer.send(datagramPacket);
                    }
                }

            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (UserNotExistException ex){
            System.out.println("Errore nel calcolo del rewars, l'utente non esiste!");
        }

    }

    /*
     * MODIFIES: this.continueLoop
     * EFFECTS: setta a false continueLoop
     */
    public void stopLoop(){
        this.continueLoop = false;
    }

    /*
     * REQUIRES: post != null
     * EFFECTS: calcola il profitto del singolo post passato come argomento
     */
    private double profitCalculation(Post post) throws UserNotExistException {
        double profit = 0;
        double like_valutation = 0, comment_valutation = 0;

        int n_iter = post.newValutation();
        Date this_calculation = last_calculation;
        last_calculation = new Date();

        //calcolo la valutazione dei like:
        HashMap<String, Integer> username_voting_map = new HashMap<>();
        for (Vote v: post.getVotes().values()) {
            if(v.getDate().after(this_calculation)){
                like_valutation += v.getRate();
                username_voting_map.putIfAbsent(v.getAuthor(), v.getRate());
            }
        }
        if(like_valutation <= 0){
            like_valutation = 1;
        }
        else{
            like_valutation++;
        }

        //calcolo la valutazione dei commenti:
        HashMap<String, Integer> username_commenting_map = new HashMap<>();
        for(Comment c: post.getComments()){
            if (c.getDate().after(this_calculation)) {
                if(username_commenting_map.containsKey(c.getAuthor())){
                    int val = username_commenting_map.get(c.getAuthor());
                    val++;
                    username_commenting_map.replace(c.getAuthor(), val);
                }
                else{
                    username_commenting_map.put(c.getAuthor(), 1);
                }

            }

        }

        int total_people_comment = 0;
        for (String username: username_commenting_map.keySet()) {
            total_people_comment = username_commenting_map.get(username);
            comment_valutation += 2/(1+Math.pow(Math.E, -(total_people_comment-1)));
        }
        comment_valutation++;

        /*
        System.out.println("Campi della funzione del profitto:");
        System.out.println("-like tot -> " + like_valutation + " in log: " + Math.log(like_valutation));
        System.out.println("-comments tot -> " + comment_valutation + " in log: " + Math.log(comment_valutation));
        System.out.println("-num interazioni -> " + n_iter);
        */

        profit = (Math.log(like_valutation)+Math.log(comment_valutation))/n_iter;
        //System.out.println("Profitto per il post num " + post.getId() + " di " + post.getAuthor() + ": " + profit);

        if(profit != 0){
            social.getWallet(post.getAuthor()).addTransaction(profit*0.7);

            HashSet<String> voting_and_commenting_users = new HashSet<>();
            voting_and_commenting_users.addAll(username_voting_map.keySet());
            voting_and_commenting_users.addAll(username_commenting_map.keySet());

            voting_and_commenting_users.remove(post.getAuthor());

            if(voting_and_commenting_users.size() != 0){
                double curators_rewards = profit*0.3/voting_and_commenting_users.size();

                for (String s: voting_and_commenting_users) {
                    social.getWallet(s).addTransaction(curators_rewards);
                }
            }
        }



        return profit;
    }

}
