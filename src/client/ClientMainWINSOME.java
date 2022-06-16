package client;

import server.RegistrationRMIInterface;
import shared.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe main del client in cui avviene la sua inizializzazzione e in cui è presente il ciclo principale della CLI con invio dei messaggi al server
 */
public class ClientMainWINSOME {
    public static ArrayList<String> follower = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Avvio client in corso...");

        //validazione del file di config
        File file;
        if(args.length < 1){//se non è stato passato un file di config uso quello di default
            file = new File("src\\config.json");
            System.out.println("Client avviato con la configurazione di default.");
        }
        else{//altrimenti leggo il file json passato per argomento
            file = new File(args[0]);

            if(!file.exists()){
                file = new File("src\\config.json");
                System.out.println("Client avviato con la configurazione di default.");
            }
            else{
                System.out.println("Client avviato con la configurazione data da \"" + args[0] + "\"");
            }
        }

        //lettura del file di config e conversione in oggetto java:
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigWINSOME config;
        try {
            config = objectMapper.readValue(file, ConfigWINSOME.class);
        }
        catch (Exception e){
            throw new RuntimeException("ERRORE: file di config del client -> " + e.getMessage());
        }

        //inizializzazione e lancio del thread che riceve i datagrammi UDP multicast dei rewards:
        RewardsNotification rewardsNotification = new RewardsNotification(config.getMulticast_address(), config.getServer_rmi_port());
        Thread rewardsNotificationThread = new Thread(rewardsNotification);
        rewardsNotificationThread.start();

        //inizializzazione della connessione con java NIO
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        SocketChannel socketChannel;
        try{
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(config.getAddress(), config.getPort()));
            socketChannel.configureBlocking(true);
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        System.out.println("Connessione stabilita con il server su " + config.getAddress() + "/" + config.getPort());
        try {
            System.out.println("Avvio CLI...");
            while (true) { //ciclo principale del client
                System.out.print("> ");

                //leggo da standard input
                String line_read = bufferedReader.readLine();
                if (line_read.equals("")) {
                    continue;
                }

                //parsing della linea letta in un arraylist con ogni elemento una parola
                ArrayList<String> line_parsed = new ArrayList<>();
                Collections.addAll(line_parsed, line_read.split(" "));

                //la testa della lista corrisponde al comando:
                String option = line_parsed.remove(0);

                if(option.equals("register")){//caso comando = register
                    if (line_parsed.size() < 2 || line_parsed.size() > 7) { //controllo di avere il numero di argomenti corretto
                        System.out.println("Opzione non corretta, per aiuto digitare help.");
                        break;
                    }

                    //estraggo i tag dagli argomenti
                    ArrayList<String> tags = new ArrayList<>(line_parsed.subList(2, line_parsed.size()));

                    //inizializzo la chiamata RMI
                    Registry registry;
                    RegistrationRMIInterface registrationRMI;
                    try {
                        registry = LocateRegistry.getRegistry(config.getAddress(), config.getServer_rmi_port());
                        registrationRMI = (RegistrationRMIInterface) registry.lookup(config.getServer_rmi_name());
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                        return;
                    }

                    //chiamata RMI
                    registrationRMI.register(line_parsed.get(0), line_parsed.get(1), tags);

                    continue;
                }

                if(option.equals("help")){ //caso comando == help
                    System.out.println("Lista delle possibili operazioni:");
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
                    continue;
                }

                // qualsiasi altro comando si comporta inviando un messaggio al server
                ByteBuffer request = ByteBuffer.wrap(new byte[line_read.length()+Integer.BYTES]);
                request.putInt(line_read.length());
                request.put(line_read.getBytes());
                request.flip();

                //invio del mesasggio
                socketChannel.write(request);
                request.clear();

                //inizializzo i bytebuffer per la risposta dal server:
                ByteBuffer response_lenght, response;
                response_lenght = ByteBuffer.allocate(Integer.BYTES);
                socketChannel.read(response_lenght);

                response_lenght.flip();
                int msg_lenght = response_lenght.getInt();
                response = ByteBuffer.allocate(msg_lenght);
                socketChannel.read(response);
                response.flip();

                //traduco la risposta in stringa
                String line_write = new String(response.array());

                //controllo del codice di risposta:
                if(line_write.equals("ok")){
                    System.out.println("Operazione avvenuta con successo!");
                }
                else if(line_write.equals("exit")){
                    socketChannel.close();
                    break;
                }
                else{
                    System.out.println("Operazione non avvenuta: " + line_write);
                }

            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Terminazione del client in corso...");
        rewardsNotification.setClientClose();
    }
}
