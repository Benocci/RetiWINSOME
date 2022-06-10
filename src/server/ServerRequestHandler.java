package server;

import shared.ConfigWINSOME;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;

public class ServerRequestHandler implements Runnable {
    ConfigWINSOME config;
    SocialNetwork social;
    SocketChannel channel;
    String request;

    public ServerRequestHandler(ConfigWINSOME config,SocialNetwork social, SocketChannel channel, String request){
        this.config = config;
        this.channel = channel;
        this.social = social;
        this.request = request;
    }


    @Override
    public void run() {
        System.out.println("Avvio thread");

        try {
            System.out.println("Messaggio ricevuto dal client: " + channel.getRemoteAddress() + ": " + request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> line_parsed = new ArrayList<>();
        Collections.addAll(line_parsed, request.split(" "));
        String option = line_parsed.remove(0);

        switch (option){
            case "login":{


            }
            case "logout":{

            }
            case "list":{

            }
            case "follow":{

            }
            case "unfollow":{

            }
            case "blog":{

            }
            case "post":{

            }
            case "show":{

            }
            case "delete":{

            }
            case "rewin":{

            }
            case "rate":{

            }
            case "comment":{

            }
            case "wallet":{

            }
            default:{

            }
        }
    }
}
