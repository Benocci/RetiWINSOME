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
    private static String username; //stringa con username dell'utente loggato su questo client, se non loggato username = null.
    public static ArrayList<String> followers = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        //validazione del file di config
        File file;
        if(args.length < 1){//se non è stato passato un file di config lo richiedo
            System.out.println("Passare un file json di configurazione del client.");
            return;
        }
        else{//altrimenti leggo il file json passato per argomento
            file = new File(args[0]);

            if(!file.exists()){
                System.out.println("File di configurazione \"" + args[0] + "\" non valido, riprovare con un altro file.");
                return;
            }
        }


        //lettura del file di config e conversione in oggetto java:
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigClientWINSOME config;
        try {
            config = objectMapper.readValue(file, ConfigClientWINSOME.class);
        }
        catch (Exception e){
            throw new RuntimeException("Errore nella lettura del file di config: " + e.getMessage());
        }

        //System.out.println("Client avviato con la configurazione data da \"" + args[0] + "\"");


        //inizializzazione della connessione con java NIO
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        SocketChannel socketChannel;

        boolean connect = false;
        socketChannel = SocketChannel.open();//apertura del canale
        while(!connect){
            try{
                //connessione su indirizzo e porta specificati dal file di config
                socketChannel.connect(new InetSocketAddress(config.getServer_address(), config.getServer_port()));
                socketChannel.configureBlocking(true);
                connect = true;
            }
            catch (IOException e){ //in caso di eccezzioni di tipo I/O (connect non riuscita) chiudo la connessione e chiedo se riaprirla
                socketChannel.close();
                System.out.print("Connessione non riuscita, vuoi riprovare? [si/no]: ");
                Scanner scanner = new Scanner(System.in);

                String reConnection = scanner.nextLine();

                if(reConnection.contains("no")){ // chiudo la connessione e termino il client
                    System.out.println("Chiusura connessione in corso!");
                    socketChannel.close();

                    System.out.println("Client terminato!");
                    return;
                }
                else{//riapro la connessione e riprovo la connect
                    socketChannel = SocketChannel.open();
                }
            }
        }

        //inizializzazione e lancio del thread che riceve i datagrammi UDP multicast dei rewards:
        RewardsNotification rewardsNotification = new RewardsNotification(config.getMulticast_address(), config.getMulticast_port());
        Thread rewardsNotificationThread = new Thread(rewardsNotification);
        rewardsNotificationThread.start();

        //inizializzo le interfacce per il callback
        ServerCallbackInterface server = null;
        NotifyEventInterface callbackObj = null;
        NotifyEventInterface stub = null;

        System.out.println("Connessione stabilita con il server su " + config.getServer_address() + "/" + config.getServer_port());
        System.out.println("Benvenuto su WINSOME, digitare help per la lista dei comandi.");

        try {
            boolean continueLoop = true;
            while (continueLoop) { //ciclo principale del client
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
                    if (line_parsed.size() <= 2 || line_parsed.size() > 7) { //controllo di avere il numero di argomenti corretto
                        System.out.println("Numero argomenti errato, per aiuto digitare help.");
                        continue;
                    }

                    if(username != null){ // controllo se il client non è già loggato sul server
                        System.out.println("Non puoi registrare un nuovo utente mentre sei loggato!");
                        continue;
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

                    //chiamata RMI per la ricezione di eventi di follow
                    registrationRMI.register(line_parsed.get(0), line_parsed.get(1), tags);

                    System.out.println("< " + line_parsed.get(0) + " benvenuto su WINSOME!");

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

                //controllo preliminare sul comando
                if(!(
                        option.equals("exit") || option.equals("login") || option.equals("logout") ||
                        option.equals("list") || option.equals("follow") || option.equals("unfollow") ||
                        option.equals("blog") || option.equals("post") || option.equals("show") || option.equals("delete") ||
                        option.equals("rewin") || option.equals("rate") || option.equals("comment") || option.equals("wallet")
                )){
                    System.out.println("Opzione non riconosciuta, per aiuto digitare help.");
                    continue;
                }


                //serie di controlli generici per ogni comandi
                if(option.equals("login") && line_parsed.size() != 2) { //controllo di avere il numero di argomenti corretto
                    System.out.println("Argomenti non sufficienti, formato corretto: login <username> <password>.");
                    continue;
                }
                else if(option.equals("logout") && line_parsed.size() != 0){
                    System.out.println("Troppi argomenti, formato corretto: logout.");
                    continue;
                }
                else if(option.equals("list")){
                    if(line_parsed.size() != 1){
                        System.out.println("Numero argomenti errato, formato corretto: list user/followers/following.");
                        continue;
                    }

                    if(line_parsed.get(0).equals("followers")){
                        if(username != null) {
                            if (followers.isEmpty()) {
                                System.out.println("< Non ti segue nessuno.");
                            } else {
                                int size = followers.size();
                                System.out.print("< Lista seguaci: ");
                                for (String s : followers) {
                                    if (size > 1) {
                                        System.out.print(s + ", ");
                                    } else {
                                        System.out.println(s + ".");
                                    }
                                    size--;
                                }
                            }
                        }
                        else{
                            System.out.println("< utente non loggato");
                        }
                        continue;
                    }

                    if(! (line_parsed.get(0).equals("following") || line_parsed.get(0).equals("users"))){
                        System.out.println("Non esiste la lista richiesta, formato corretto: list user/followers/following.");
                        continue;
                    }
                }
                else if(option.equals("follow") && line_parsed.size() != 1){
                    System.out.println("Numero argomenti errato, formato corretto: follow <username>.");
                    continue;
                }
                else if(option.equals("unfollow") && line_parsed.size() != 1){
                    System.out.println("Numero argomenti errato, formato corretto: unfollow <username>.");
                    continue;
                }
                else if(option.equals("post")){
                    if(line_read.chars().filter(ch -> ch == '"').count() != 4){
                        System.out.println("Sintassi del comando errata, formato corretto: post \"<titolo>\" \"<contenuto>\".");
                        break;
                    }
                }
                else if(option.equals("blog") && line_parsed.size() != 0){
                    System.out.println("Numero argomenti errato, formato corretto: blog.");
                    continue;
                }
                else if(option.equals("show")){
                    if(line_parsed.size() < 1){
                        System.out.println("Numero argomenti errato, formato corretto: show feed / show post <id>");
                        continue;
                    }

                    if(! (line_parsed.get(0).equals("feed") || line_parsed.get(0).equals("post"))){
                        System.out.println("Non esiste il comando richiesto, formato corretto: show feed / show post <id>.");
                        continue;
                    }
                }
                else if(option.equals("delete") && line_parsed.size() != 1){
                    System.out.println("Numero argomenti errato, formato corretto: delete <idPost>.");
                    continue;
                }
                else if(option.equals("comment")){
                    if(line_read.chars().filter(ch -> ch == '"').count() != 2){
                        System.out.println("Sintassi del comando errata, formato corretto: comment <idPost> \"<contenuto>\".");
                        break;
                    }
                }
                else if(option.equals("rewin") && line_parsed.size() != 1){
                    System.out.println("Numero argomenti errato, formato corretto: rewin <idPost>.");
                    continue;
                }
                else if(option.equals("rate") && line_parsed.size() != 2){
                    System.out.println("Numero argomenti errato, formato corretto: rate <idPost> <vote>.");
                    continue;
                }
                else if(option.equals("wallet")){
                    if(line_parsed.size() == 1 && !line_parsed.get(0).equals("btc")){
                        System.out.println("Non esiste il comando richiesto, formato corretto: wallet / wallet btc.");
                        continue;
                    }

                    if(line_parsed.size() >= 2){
                        System.out.println("Numero argomenti errato, formato corretto: wallet / wallet btc.");
                        continue;
                    }
                }

                if(option.equals("exit")){ // in caso di exit invio comunque al server il comando ma imposto la fine del ciclo del client
                    continueLoop = false;
                }

                // invio del messaggio al server con java NIO:

                //creo il buffer di byte:
                ByteBuffer request = ByteBuffer.wrap(new byte[line_read.length()+Integer.BYTES]);
                request.putInt(line_read.length());
                request.put(line_read.getBytes());
                request.flip();

                //invio del buffer di byte contenente il messaggio:
                socketChannel.write(request);
                request.clear();

                //inizializzo i bytebuffer per la risposta dal server:
                ByteBuffer response_lenght, response;
                response_lenght = ByteBuffer.allocate(Integer.BYTES);
                //leggo la lunghezza del messaggio in arrivo
                socketChannel.read(response_lenght);
                response_lenght.flip();
                int msg_lenght = response_lenght.getInt();
                //alloco il bytebuffer della lunghezza indicata e su di esso leggo il messaggio di risposta del server:
                response = ByteBuffer.allocate(msg_lenght);
                socketChannel.read(response);
                response.flip();

                //traduco la risposta in stringa
                String line_write = new String(response.array());

                //stampo la risposta del server:
                System.out.println("< " + line_write);

                if(option.equals("login") && line_write.equals("ok")){//in caso di login effettuato correttamente setto username e registro il client per le callback
                    try {
                        Registry registry = LocateRegistry.getRegistry(config.getRmi_callback_port());
                        server = (ServerCallbackInterface) registry.lookup(config.getRmi_callback_name());
                        callbackObj = new NotifyEvent();
                        stub = (NotifyEventInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
                        username = line_parsed.get(0);
                        server.registerForCallback(username,stub);
                        followers = server.getFollowersList(username);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                if(option.equals("logout") && line_write.equals("ok") && server != null){//in caso di logout effettuato correttamente tolgo il client dalla callback
                    try{
                        server.unregisterForCallback(username);
                    }
                    catch (RemoteException e){
                        e.printStackTrace();
                    }
                    username = null;
                    followers = new ArrayList<>();
                }

            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        //fine del client:

        //imposto la chiusura del thread che legge i rewards:
        rewardsNotification.setClientClose();

        while(!rewardsNotificationThread.isInterrupted()){
            System.out.println("DEBUG: Interrompo il thread.");
            rewardsNotificationThread.interrupt();//interrompo il thread
        }

        try{
            if(username != null && server != null){ // se è stata fatta una exit con un client loggato tolgo dalla registrazione della callback
                server.unregisterForCallback(username);
            }

            if(callbackObj != null){
                UnicastRemoteObject.unexportObject(callbackObj, false);
            }
        }catch (RemoteException ignore){
            ;
        }

        socketChannel.close(); // chiudo la connessione

        System.out.println("Client terminato!");
    }
}
