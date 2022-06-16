package server;

import shared.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW:
 */
public class RewardsCalculation implements Runnable{
    ConfigWINSOME config;
    SocialNetwork social;

    private volatile Boolean continueLoop = true;

    public RewardsCalculation(ConfigWINSOME config, SocialNetwork social){
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

        try (DatagramSocket datagramSocketServer = new DatagramSocket(config.getMulticast_port()+1)) {
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

            try{
                Thread.sleep(config.getReward_timeout());
            }
            catch (InterruptedException ignore){
                ;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    public void stopLoop(){
        this.continueLoop = false;
    }

    /*
     * REQUIRES:
     * MODIFIES:
     * EFFECTS:
     * THROWS:
     */
    private double profitCalculation(Post post){
        return post.getVote();
    }



}
