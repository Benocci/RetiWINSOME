package client;

import server.RegistrationRMIInterface;

import com.fasterxml.jackson.databind.ObjectMapper;
import server.ServerCallbackInterface;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe main del client in cui avviene la sua inizializzazzione e in cui è presente il ciclo principale della CLI con invio dei messaggi al server
 */
public class ClientMainWINSOME {
    private static String username;
    public static ArrayList<String> follower = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Avvio client in corso...");

        //validazione del file di config
        File file;
        if(args.length < 1){//se non è stato passato un file di config uso quello di default
            file = new File("src\\configFile\\configClient.json");
            System.out.println("Client avviato con la configurazione di default.");
        }
        else{//altrimenti leggo il file json passato per argomento
            file = new File(args[0]);

            if(!file.exists()){
                file = new File("src\\configFile\\configClient.json");
                System.out.println("Client avviato con la configurazione di default.");
            }
            else{
                System.out.println("Client avviato con la configurazione data da \"" + args[0] + "\"");
            }
        }

        //lettura del file di config e conversione in oggetto java:
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigClientWINSOME config;
        try {
            config = objectMapper.readValue(file, ConfigClientWINSOME.class);
        }
        catch (Exception e){
            throw new RuntimeException("ERRORE: file di config del client -> " + e.getMessage());
        }

        //inizializzazione e lancio del thread che riceve i datagrammi UDP multicast dei rewards:
        RewardsNotification rewardsNotification = new RewardsNotification(config.getMulticast_address(), config.getMulticast_port());
        Thread rewardsNotificationThread = new Thread(rewardsNotification);
        rewardsNotificationThread.start();

        //inizializzazione della connessione con java NIO
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        SocketChannel socketChannel;

        boolean connect = false;
        socketChannel = SocketChannel.open();
        while(!connect){
            try{
                socketChannel.connect(new InetSocketAddress(config.getServer_address(), config.getServer_port()));
                socketChannel.configureBlocking(true);
                connect = true;
            }
            catch (IOException e){
                socketChannel.close();
                System.out.print("Connessione non riuscita, vuoi riprovare? [si/no]: ");
                Scanner scanner = new Scanner(System.in);

                String reConnection = scanner.nextLine();

                if(reConnection.contains("no")){
                    System.out.println("Chiusura connessione in corso!");
                    return;
                }
                else{
                    socketChannel = SocketChannel.open();
                }
            }
        }

        //inizializzo le interfacce per il callback
        ServerCallbackInterface server = null;
        NotifyEventInterface callbackObj = null;
        NotifyEventInterface stub = null;

        System.out.println("Connessione stabilita con il server su " + config.getServer_address() + "/" + config.getServer_port());
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
                        registry = LocateRegistry.getRegistry(config.getServer_address(), config.getServer_registryRMI_port());
                        registrationRMI = (RegistrationRMIInterface) registry.lookup(config.getServer_registryRMI_name());
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

                if(line_read.contains("exit")){
                    break;
                }

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
                System.out.println("< " + line_write);

                if(option.equals("login") && line_write.equals("ok")){
                    try {
                        Registry registry = LocateRegistry.getRegistry(config.getRmi_callback_port());
                        server = (ServerCallbackInterface) registry.lookup(config.getRmi_callback_name());
                        callbackObj = new NotifyEvent();
                        stub = (NotifyEventInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
                        username = line_parsed.get(0);
                        server.registerForCallback(username,stub);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                if(option.equals("logout") && line_write.equals("ok") && server != null){
                    try{
                        server.unregisterForCallback(username);
                    }
                    catch (RemoteException e){
                        e.printStackTrace();
                    }
                    username = null;

                }

            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Terminazione del client in corso...");
        rewardsNotification.setClientClose();
        try{
            if(username != null && server != null){
                server.unregisterForCallback(username);
            }

            if(callbackObj != null){
                UnicastRemoteObject.unexportObject(callbackObj, false);
            }
        }catch (RemoteException ignore){
            ;
        }
        socketChannel.close();

        if(rewardsNotificationThread.isInterrupted()){
            System.out.println("Client terminato correttamente.");
        }
        else{
            System.out.println("Interrompo il thread di notifica!");
            rewardsNotificationThread.interrupt();
        }
    }
}
