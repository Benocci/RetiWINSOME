package client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe runnable che implementa la ricezione dei rewards da parte del server
 */
public class RewardsNotification implements Runnable{
    private MulticastSocket multicastSocket;
    private InetAddress inetAddress;
    private int port;
    private Boolean clientClose = false;

    /*
     * REQUIRES: address != null ∧ port > 0
     * MODIFIES: this
     * THROWS: UnknownHostException: se non trova indirizzi IP per l'host
     */
    public RewardsNotification(String address, int port){
        try{
            this.inetAddress = InetAddress.getByName(address);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
    }

    /*
     * EFFECTS: override metodo run che implementa la ricezione dei reward con multicast UDP
     * THROWS: IOException se ci sono errori I/O
     */
    @Override
    public void run() {
        try {
            //inizializzazzione del multicast UDP
            multicastSocket = new MulticastSocket(port);
            InetSocketAddress group = new InetSocketAddress(inetAddress, port);
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            multicastSocket.joinGroup(group,networkInterface);

            while(true){
                try{
                    //inizializzazzione dei buffer e del datagramma
                    ByteBuffer lenghtBuffer = ByteBuffer.allocate(Integer.BYTES);
                    DatagramPacket datagramPacket = new DatagramPacket(lenghtBuffer.array(), lenghtBuffer.limit());

                    //ricezione lunghezza stringa
                    multicastSocket.receive(datagramPacket);
                    int lenght = ByteBuffer.wrap(datagramPacket.getData()).getInt();

                    //ricezione stringa
                    ByteBuffer to_receiveBuffer = ByteBuffer.allocate(lenght);
                    datagramPacket = new DatagramPacket(to_receiveBuffer.array(), to_receiveBuffer.limit());
                    multicastSocket.receive(datagramPacket);

                    String received = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());

                    System.out.println(received);

                    float rewards = Float.parseFloat(received.substring(received.indexOf(":")));
                    System.out.println("CIOE solo " + rewards);

                    if(clientClose){ // client chiuso concludo il ciclo chiudento la ricezione
                        multicastSocket.close();
                        return;
                    }
                }
                catch (IOException ignore) {}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * MODIFIES: this.clientClose
     * EFFECTS: imposta l'uscita dal ciclo del thread
     */
    public void setClientClose(){
        clientClose = true;
    }
}
