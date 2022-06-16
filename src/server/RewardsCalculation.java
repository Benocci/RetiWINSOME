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
        try ( DatagramSocket datagramSocketServer = new DatagramSocket(config.getMulticast_port()) ){
            DatagramPacket datagramPacket;

            ConcurrentHashMap<Integer, Post> postMap;

            while(continueLoop){
                postMap = social.getPostMap();
                double reward;
                double total_reward = 0;

                if(postMap.size() != 0){

                    for(Post p: postMap.values()) {
                        reward = profitCalculation(p);
                        total_reward += reward;
                    }

                    if(total_reward != 0){
                        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
                        byteBuffer.putDouble(total_reward);

                        InetAddress clientAddress = InetAddress.getByName(config.getMulticast_address());
                        datagramPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.limit(), clientAddress ,config.getMulticast_port());
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
