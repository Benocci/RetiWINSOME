package client;

import shared.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Scanner;


public class ClientMainWINSOME {

    public static void main(String[] args) {
        System.out.println("Avvio client in corso...");

        File file;
        if(args.length < 1){
            file = new File("src\\config.json");
            System.out.println("Client avviato con la configurazione di default.");
        }
        else{
            file = new File(args[0]);

            if(!file.exists()){
                file = new File("src\\config.json");
                System.out.println("Client avviato con la configurazione di default.");
            }
            else{
                System.out.println("Client avviato con la configurazione data da \"" + args[0] + "\"");
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ConfigWINSOME config;
        try {
            config = objectMapper.readValue(file, ConfigWINSOME.class);
        }
        catch (Exception e){
            throw new RuntimeException("ERRORE: file di config del client -> " + e.getMessage());
        }

        System.out.println("Stampo porta=" + config.getPort() + "\nStampo indirizzo=" + config.getAddress());

        String line_readed = "";
        Scanner scanner = new Scanner(System.in);
        Boolean continueLoop = true;

        System.out.println("Avvio CLI...");
        while (continueLoop){
            System.out.print("> ");

            line_readed = scanner.nextLine();
            if(line_readed.equals("")){
                continue;
            }

            String[] line_parsed = line_readed.split(" ");

            switch (line_parsed[0]){
                case "exit":{
                    continueLoop = false;
                    break;
                }
                case "help":{
                    System.out.println(" Lista delle possibili operazioni:");
                    System.out.println("exit                                   >termina il client<");
                    System.out.println("register <username> <password> <tags>  >registra un nuovo utente<");
                    System.out.println("login <username> <password>            >effettua il login<");
                    System.out.println("logout                                 >effettua il logout<");
                    System.out.println("list users                             >visualizza la lista utenti con tag in comune<");
                    System.out.println("list followers                         >visualizza la lista dei follower<");
                    System.out.println("list following                         >visualizza la lista degli utenti di cui è follower<");
                    System.out.println("follow <utente>                        >segui l'utente indicato<");
                    System.out.println("unfollow <utente>                      >smetti di seguire l'utente indicato<");
                    System.out.println("blog                                   >visualizza la lista dei propri post<");
                    System.out.println("post <titolo> <contenuto>              >crea un post<");
                    System.out.println("show feed                              >visualizza la lista dei post del proprio feed<");
                    System.out.println("show post <id>                         >visualizza il post indicato<");
                    System.out.println("delete <idPost>                        >elimina il post indicato<");
                    System.out.println("rewin <idPost>                         >rewin del post indicato<");
                    System.out.println("rate <idPost> <vote>                   >votazione del post indicato con +1/-1<");
                    System.out.println("comment <idPost> <testo>               >commento al post indicato<");
                    System.out.println("wallet                                 >valore del proprio portafoglio<");
                    System.out.println("wallet btc                             >valore del proprio portafoglio in Bitcoin<");
                    break;
                }
                case "register":
                case "login":
                case "logout":
                case "list":
                case "follow":
                case "unfollow":
                case "blog":
                case "post":
                case "show":
                case "delete":
                case "rewin":
                case "rate":
                case "comment":
                case "wallet":{
                    int i = 0;
                    for (String s: line_parsed) {
                        if(i == 0){
                            System.out.println("Operazione = " + s);
                        }
                        else{
                            System.out.println("Argomento = " + s);
                        }
                        i++;
                    }
                    break;
                }
                default:{
                    System.out.println("Opzione non trovata, per aiuto digitare help.");
                }
            }
        }

        System.out.println("Terminazione del client in corso...");
    }
}
