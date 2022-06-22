package server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW:
 */
public class RewardsCalculation implements Runnable{
    ConfigServerWINSOME config;
    SocialNetwork social;
    int rewards_period = 10000;

    private Boolean continueLoop = true;

    public RewardsCalculation(ConfigServerWINSOME config, SocialNetwork social){
        this.config = config;
        this.social = social;
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
                double reward;
                double total_reward = 0;

                try{
                    Thread.sleep(rewards_period);
                }
                catch (InterruptedException ignore){
                    ;
                }

                if(postMap.size() != 0){

                    for(Post p: postMap.values()) {
                        reward = profitCalculation(p);
                        total_reward += reward;
                    }

                    if(total_reward != 0){
                        String to_send = "Reward totale:" + total_reward;

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
    private double profitCalculation(Post post){
        return post.getVote();
    }

}
