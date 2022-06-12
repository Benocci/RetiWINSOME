package server;

import shared.*;

import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

public class ServerMainWINSOME {
    static SocialNetwork socialNetwork;
    public static final ConcurrentHashMap<String, String> loggedUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Avvio server in corso...");

        File file;
        if(args.length < 1){
            file = new File("src\\config.json");
            System.out.println("Server avviato con la configurazione di default.");
        }
        else{
            file = new File(args[0]);

            if(!file.exists()){
                file = new File("src\\config.json");
                System.out.println("Server avviato con la configurazione di default.");
            }
            else{
                System.out.println("Server avviato con la configurazione data da \"" + args[0] + "\"");
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

        socialNetwork = new SocialNetwork();

        RegistrationRMI registrationRMI = new RegistrationRMI();
        try{
            RegistrationRMIInterface registrationStub = (RegistrationRMIInterface) UnicastRemoteObject.exportObject(registrationRMI, 0);
            Registry registry = LocateRegistry.createRegistry(config.getServer_rmi_port());
            registry.rebind(config.getServer_rmi_name(), registrationStub);
        }
        catch(RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ServerCallback serverCallback = new ServerCallback();
        try{
            ServerCallbackInterface callbackStub = (ServerCallbackInterface) UnicastRemoteObject.exportObject(serverCallback, 0);
            Registry registry = LocateRegistry.createRegistry(config.getServer_rmi_port()+1);
            registry.rebind(config.getServer_rmi_name(), callbackStub);
        }
        catch(RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        System.out.println("Stampo porta=" + config.getPort() + "\nStampo indirizzo=" + config.getAddress());

        Selector selector = null;
        try{
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(config.getPort()));
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

                        System.out.println("Connessione stabilita con il client: " + socketChannel.getRemoteAddress());
                        socketChannel.register(selector, SelectionKey.OP_READ, null);
                    }
                    if(selectionKey.isReadable()){
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        socketChannel.configureBlocking(false);

                        ByteBuffer request;
                        if(selectionKey.attachment() == null){
                            ByteBuffer request_lenght = ByteBuffer.allocate(Integer.BYTES);
                            var nread = socketChannel.read(request_lenght);
                            assert (nread == Integer.BYTES);
                            request_lenght.flip();
                            int msgLen = request_lenght.getInt();
                            System.out.println("Lunghezza messaggio: " + msgLen);

                            request = ByteBuffer.allocate(msgLen);
                            selectionKey.attach(request);
                        }

                        request = (ByteBuffer) selectionKey.attachment();
                        int readBytes = socketChannel.read(request);
                        if(readBytes < 0){
                            socketChannel.close();
                            throw new IOException("Connessione chiusa!");
                        }

                        request.flip();
                        String to_read = new String(request.array());
                        System.out.println("Arrivato: " + to_read + " lungo: " + to_read.length());

                        request.clear();
                        selectionKey.attach(null);


                        if (to_read.contains("exit")) {
                            socketChannel.close();
                            System.out.println("Connessione conclusa!");
                            break;
                        }

                        threadPool.execute(new ServerRequestHandler(config, socialNetwork, socketChannel.getRemoteAddress().toString(), to_read.toString()));
                    }
                    else if(selectionKey.isWritable()){
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer response = (ByteBuffer) selectionKey.attachment();

                        socketChannel.write(response);
                        response.clear();

                        socketChannel.register(selector, SelectionKey.OP_READ, null);
                    }
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
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

        try{
            UnicastRemoteObject.unexportObject(registrationRMI, false);
            UnicastRemoteObject.unexportObject(serverCallback, false);
        }
        catch (NoSuchObjectException e){
            e.printStackTrace();
        }

    }
}
