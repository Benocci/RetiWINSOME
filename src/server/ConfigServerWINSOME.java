package server;


public class ConfigServerWINSOME {
    private String server_address;
    private int server_port;
    private String rmi_registration_name;
    private int rmi_registration_port;
    private String multicast_address;
    private int multicast_port;
    private String rmi_callback_name;
    private int rmi_callback_port;
    private int rewards_timeout;

    public ConfigServerWINSOME(String server_address, int server_port, String rmi_registration_name, int rmi_registration_port, String multicast_address, int multicast_port, String rmi_callback_name, int rmi_callback_port, int rewards_timeout) {
        this.server_address = server_address;
        this.server_port = server_port;
        this.rmi_registration_name = rmi_registration_name;
        this.rmi_registration_port = rmi_registration_port;
        this.multicast_address = multicast_address;
        this.multicast_port = multicast_port;
        this.rmi_callback_name = rmi_callback_name;
        this.rmi_callback_port = rmi_callback_port;
        this.rewards_timeout = rewards_timeout;
    }

    public ConfigServerWINSOME(){

    }

    public String getServer_address() {
        return server_address;
    }

    public int getServer_port() {
        return server_port;
    }

    public String getRmi_registration_name() {
        return rmi_registration_name;
    }

    public int getRmi_registration_port() {
        return rmi_registration_port;
    }

    public String getMulticast_address() {
        return multicast_address;
    }

    public int getMulticast_port() {
        return multicast_port;
    }

    public String getRmi_callback_name() {
        return rmi_callback_name;
    }

    public int getRmi_callback_port() {
        return rmi_callback_port;
    }

    public int getRewards_timeout() {
        return rewards_timeout;
    }
}
