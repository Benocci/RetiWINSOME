package server;

import shared.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class RewardsCalculation implements Runnable{
    ConfigWINSOME config;
    SocialNetwork social;

    private volatile boolean continueLoop = true;

    public RewardsCalculation(ConfigWINSOME config, SocialNetwork social){
        this.config = config;
        this.social = social;
    }

    @Override
    public void run() {

        try (DatagramSocket datagramSocketServer = new DatagramSocket(null)) {
            InetAddress inetAddress = InetAddress.getLocalHost();
            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, config.getMulticast_port());
            datagramSocketServer.setReuseAddress(true);
            datagramSocketServer.bind(socketAddress);

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
                        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
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


    private double profitCalculation(Post post){
        return post.getVote();
    }



}
