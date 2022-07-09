package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BackupManager implements Runnable{
    private final long backup_period = 1000;
    private final String directory = "src/backupServerState";
    private final String user_path = "usersBackup.json";
    private final String follower_path = "followerBackup.json";
    private final String following_path = "followedBackup.json";
    private final String post_path = "postBackup.json";
    private SocialNetwork social;
    private final Gson gson;
    private Boolean stopBackup;

    public BackupManager(SocialNetwork social){
        this.social = social;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.stopBackup = false;
    }

    @Override
    public void run() {

        while(true){
            try{
                Thread.sleep(backup_period);
            }
            catch (InterruptedException ignore){
                ;
            }

            if(stopBackup){
                return;
            }

            saveInJson(directory + "/" + user_path, social.getUsers());
            saveInJson(directory + "/" + follower_path, social.getFollowersMap());
            saveInJson(directory + "/" + following_path, social.getFollowingMap());
            saveInJson(directory + "/" + post_path, social.getPostMap());
        }
    }

    public void stopBackup() {
        this.stopBackup = true;
    }

    public void loadBackup(){
        String usersMap = readJson(directory + "/" + user_path);
        String followersMap = readJson(directory + "/" + follower_path);
        String followingMap = readJson(directory + "/" + following_path);
        String postMap = readJson(directory + "/" + post_path);

        if(usersMap != null || usersMap.equals("")){
            Type type = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
            social.setUsers(gson.fromJson(usersMap, type));
        }

        if(followersMap != null || followersMap.equals("")){
            Type type = new TypeToken<ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>>() {}.getType();
            social.setFollowersMap(gson.fromJson(followersMap, type));
        }

        if(followingMap != null || followingMap.equals("")){
            Type type = new TypeToken<ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>>() {}.getType();
            social.setFollowingMap(gson.fromJson(followingMap, type));
        }

        if(postMap != null || postMap.equals("")){
            Type type = new TypeToken<ConcurrentHashMap<Integer, Post>>() {}.getType();
            social.setPostMap(gson.fromJson(postMap, type));
        }
    }

    private String readJson(String path){
        ReadableByteChannel source;

        try{
            source = Channels.newChannel(new FileInputStream(path));
        }
        catch (FileNotFoundException e){
            new File(directory).mkdirs();

            File file = new File(path);
            try{
                file.createNewFile();
            }
            catch (IOException ex){
                e.printStackTrace();
                return null;
            }

            try{
                source = Channels.newChannel(new FileInputStream(path));
            }
            catch (FileNotFoundException e1){
                e1.printStackTrace();
                return null;
            }
        }

        ByteBuffer to_read = ByteBuffer.allocateDirect(2048);
        StringBuilder input = new StringBuilder();
        to_read.clear();

        try{
            while (source.read(to_read) >= 0 || to_read.position() != 0){
                to_read.flip();

                while(to_read.hasRemaining()){
                    input.append((char) to_read.get());
                }

                to_read.compact();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        to_read.flip();
        while (to_read.hasRemaining()){
            input.append((char) to_read.get());
        }

        try{
            source.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return input.toString();
    }

    private void saveInJson(String path, Object structure){
        String to_save = gson.toJson(structure);

        WritableByteChannel destination;
        try {
            destination = Channels.newChannel(new FileOutputStream(path));
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }

        ByteBuffer to_write = ByteBuffer.allocateDirect(to_save.getBytes().length);
        to_write.put(to_save.getBytes());
        to_write.flip();
        try{
            destination.write(to_write);
            destination.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
