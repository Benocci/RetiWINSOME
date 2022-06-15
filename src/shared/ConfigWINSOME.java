package shared;

public class ConfigWINSOME {
    private String address;
    private int port;
    private String server_rmi_name;
    private int server_rmi_port;
    private String multicast_address;
    private int multicast_port;
    private int reward_timeout;

    public ConfigWINSOME(String address, int port, String server_rmi_name, String multicast_address, int server_rmi_port, int multicast_port, int reward_timeout){
        this.port = port;
        this.address = address;
        this.server_rmi_name = server_rmi_name;
        this.server_rmi_port = server_rmi_port;
        this.multicast_address = multicast_address;
        this.multicast_port = multicast_port;
        this.reward_timeout = reward_timeout;
    }

    public ConfigWINSOME(){

    }

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
