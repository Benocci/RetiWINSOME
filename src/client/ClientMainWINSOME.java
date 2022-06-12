package client;

import server.RegistrationRMI;
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


public class ClientMainWINSOME {
    public static ArrayList<String> follower = new ArrayList<>();

    public static void main(String[] args) throws IOException {
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

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        SocketChannel socketChannel = null;
        try{
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(config.getAddress(), config.getPort()));
            socketChannel.configureBlocking(true);
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        System.out.println("Connessione stabilita con il server sulla porta " + config.getPort());
        try {
            System.out.println("Avvio CLI...");
            while (true) {
                System.out.print("> ");

                String line_read = bufferedReader.readLine();
                if (line_read.equals("")) {
                    continue;
                }

                ArrayList<String> line_parsed = new ArrayList<>();
                Collections.addAll(line_parsed, line_read.split(" "));
                String option = line_parsed.remove(0);

                if(option.equals("register")){
                    if (line_parsed.size() < 2 || line_parsed.size() > 7) {
                        System.out.println("Opzione non corretta, per aiuto digitare help.");
                        break;
                    }

                    ArrayList<String> tags = new ArrayList<>();
                    tags.addAll(line_parsed.subList(2, line_parsed.size()));

                    Registry registry;
                    RegistrationRMIInterface registrationRMI;
                    try {
                        registry = LocateRegistry.getRegistry(config.getAddress(), config.getServer_rmi_port());
                        registrationRMI = (RegistrationRMIInterface) registry.lookup(config.getServer_rmi_name());
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                        return;
                    }

                    registrationRMI.register(line_parsed.get(0), line_parsed.get(1), tags);

                    continue;
                }

                if(option.equals("help")){
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

                ByteBuffer request = ByteBuffer.wrap(new byte[line_read.length()+Integer.BYTES]);
                request.putInt(line_read.length());

                System.out.println("Invio: " + line_read + " lungo " + line_read.length());
                request.put(line_read.getBytes());
                request.flip();
                socketChannel.write(request);
                request.clear();

                ByteBuffer response_lenght, response;
                response_lenght = ByteBuffer.allocate(Integer.BYTES);
                socketChannel.read(response_lenght);

                response_lenght.flip();
                int msg_lenght = response_lenght.getInt();
                response = ByteBuffer.allocate(msg_lenght);
                socketChannel.read(response);

                response.flip();
                String line_write = new String(response.array());

                System.out.println(line_write);

                if(option.equals("exit")){
                    socketChannel.close();
                    break;
                }

            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Terminazione del client in corso...");
    }
}
