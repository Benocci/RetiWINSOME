package client;
/*
 *  AUTORE: FRANCESCO BENOCCI matricola 602495 UNIPI
 *  OVERVIEW: classe che rappresenta un file di config per il client( viene tradotta da un file json )
 */
public class ConfigClientWINSOME {
    private String server_address;
    private int server_port;
    private String server_registryRMI_name;
    private int server_registryRMI_port;
    private String multicast_address;
    private int multicast_port;
    private String rmi_callback_name;
    private int rmi_callback_port;

    public ConfigClientWINSOME(String server_address, int server_port, String server_registryRMI_name, int server_registryRMI_port, String multicast_address, int multicast_port, String rmi_callback_name, int rmi_callback_port){
        this.server_address = server_address;
        this.server_port = server_port;
        this.server_registryRMI_name = server_registryRMI_name;
        this.server_registryRMI_port = server_registryRMI_port;
        this.multicast_address = multicast_address;
        this.multicast_port = multicast_port;
        this.rmi_callback_name = rmi_callback_name;
        this.rmi_callback_port = rmi_callback_port;
    }

    public ConfigClientWINSOME(){

    }

    public String getServer_address() {
        return server_address;
    }

    public int getServer_port() {
        return server_port;
    }

    public String getServer_registryRMI_name() {
        return server_registryRMI_name;
    }

    public int getServer_registryRMI_port() {
        return server_registryRMI_port;
    }

    public String getMulticast_address() {
        return multicast_address;
    }

    public int getMulticast_port() {
        return multicast_port;
    }

    public int getRmi_callback_port() {
        return rmi_callback_port;
    }

    public String getRmi_callback_name() {
        return rmi_callback_name;
    }
}
