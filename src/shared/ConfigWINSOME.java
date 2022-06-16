package shared;
/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta un file di config ( viene tradotta da un file json )
 */
public class ConfigWINSOME {
    private final String address;
    private final int port;
    private final String server_rmi_name;
    private final int server_rmi_port;
    private final String multicast_address;
    private final int multicast_port;
    private final int reward_timeout;

    public ConfigWINSOME(String address, int port, String server_rmi_name, String multicast_address, int server_rmi_port, int multicast_port, int reward_timeout){
        this.port = port;
        this.address = address;
        this.server_rmi_name = server_rmi_name;
        this.server_rmi_port = server_rmi_port;
        this.multicast_address = multicast_address;
        this.multicast_port = multicast_port;
        this.reward_timeout = reward_timeout;
    }

    //metodi get dei vari campi del file di config:
    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public String getServer_rmi_name() {
        return server_rmi_name;
    }

    public int getServer_rmi_port() {
        return server_rmi_port;
    }

    public String getMulticast_address() {
        return multicast_address;
    }

    public int getMulticast_port() {
        return multicast_port;
    }

    public int getReward_timeout() {
        return reward_timeout;
    }
}
