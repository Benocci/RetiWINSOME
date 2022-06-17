package server;

import com.google.gson.Gson;

public class BackupManager implements Runnable{
    private long backup_period = 1000;
    private SocialNetwork social;
    private final Gson gson;

    public BackupManager(SocialNetwork social){
        this.social = social;
        this.gson = new Gson();
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


        }
    }
}
