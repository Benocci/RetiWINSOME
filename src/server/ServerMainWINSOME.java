package server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW:
 */
public class ServerMainWINSOME {
    static SocialNetwork socialNetwork;
    public static final ConcurrentHashMap<String, String> loggedUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        File file;
        if(args.length < 1){
            System.out.println("Passare un file di config per il server.");
            return;
        }
        else{
            file = new File(args[0]);

            if(!file.exists()){
                System.out.println("File di configurazione non valido, riprovare con un altro file.");
                return;
            }
        }

        //lettura del file di config json e traduzione in classe java
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigServerWINSOME config;
        try {
            config = objectMapper.readValue(file, ConfigServerWINSOME.class);
        }
        catch (Exception e){
            throw new RuntimeException("ERRORE: file di config del client -> " + e.getMessage());
        }

        System.out.println("Server avviato con la configurazione data da \"" + args[0] + "\"");

        //inizializzo il socialnetwork
        socialNetwork = new SocialNetwork();

        //carico il backup precedente:
        BackupManager backupManager = new BackupManager(socialNetwork);
        backupManager.loadBackup();

        //inizializzo e avvio il sistema di backup:
        Thread backupThread = new Thread(backupManager);
        backupThread.start();

        //inizializzo e avvio il sistema di rewards
        RewardsCalculation rewardsCalculation = new RewardsCalculation(config, socialNetwork);
        Thread rewardsThread = new Thread(rewardsCalculation);
        rewardsThread.start();

        //inizializzazione del sisema rmi per la registrazione degli utenti
        RegistrationRMI registrationRMI = new RegistrationRMI();
        try{
            RegistrationRMIInterface registrationStub = (RegistrationRMIInterface) UnicastRemoteObject.exportObject(registrationRMI, 0);
            Registry registry = LocateRegistry.createRegistry(config.getRmi_registration_port());
            registry.rebind(config.getRmi_registration_name(), registrationStub);
        }
        catch(RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //inizializzazione del sistema di callback con rmi ai client
        ServerCallback serverCallback = new ServerCallback();
        try{
            ServerCallbackInterface callbackStub = (ServerCallbackInterface) UnicastRemoteObject.exportObject(serverCallback, 39000);
            Registry registry = LocateRegistry.createRegistry(config.getRmi_callback_port());
            registry.rebind(config.getRmi_callback_name(), callbackStub);
        }
        catch(RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        //inizializzazione del sistema java NIO su cui girerà la comunicazione tcp principale (multiplexing)
        Selector selector = null;
        try{
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(config.getServer_port()));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        while (true){
            try {
                if (selector.select() == 0) continue;
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);

                        System.out.println(" -+- Connessione stabilita con il client: " + socketChannel.getRemoteAddress());
                        socketChannel.register(selector, SelectionKey.OP_READ, null);
                    }
                    if(selectionKey.isReadable()){
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        socketChannel.configureBlocking(false);

                        ByteBuffer request;
                        if(selectionKey.attachment() == null){
                            ByteBuffer request_lenght = ByteBuffer.allocate(Integer.BYTES);
                            int nread = socketChannel.read(request_lenght);
                            assert (nread == Integer.BYTES);
                            request_lenght.flip();
                            int msgLen = request_lenght.getInt();

                            request = ByteBuffer.allocate(msgLen);
                            selectionKey.attach(request);
                        }

                        request = (ByteBuffer) selectionKey.attachment();
                        int readBytes = socketChannel.read(request);
                        if(readBytes < 0){
                            socketChannel.close();
                            throw new IOException("Connessione chiusa!");
                        }


                        if(!request.hasRemaining()){
                            request.flip();
                            String read = new String(request.array());

                            threadPool.execute(new ServerRequestHandler(socialNetwork, socketChannel.getRemoteAddress().toString(), socketChannel, selector, read, serverCallback));
                            request.clear();
                        }
                    }
                    else if(selectionKey.isWritable()){
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer response = (ByteBuffer) selectionKey.attachment();

                        if(response.hasRemaining()){
                            socketChannel.write(response);
                        }

                        if(!response.hasRemaining()){
                            String res = new String(response.array());
                            if(res.contains("exit")){
                                try {
                                    System.out.println(" --- Client " + socketChannel.getRemoteAddress()  + " disconnesso.");
                                    socketChannel.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                socketChannel.register(selector, SelectionKey.OP_READ, null);
                            }
                        }
                    }
                }
            }
            catch (IOException ex){
                System.out.println("Connessione con il client persa!");
                break;
            }
        }

        threadPool.shutdown();
        try {
            if(!threadPool.awaitTermination(3, TimeUnit.SECONDS)){
                threadPool.shutdownNow();
            }
        }
        catch (InterruptedException e){
            threadPool.shutdownNow();
        }

        rewardsCalculation.stopLoop();
        backupManager.stopBackup();

        try{
            UnicastRemoteObject.unexportObject(registrationRMI, false);
            UnicastRemoteObject.unexportObject(serverCallback, false);
        }
        catch (NoSuchObjectException e){
            e.printStackTrace();
        }

    }

}
