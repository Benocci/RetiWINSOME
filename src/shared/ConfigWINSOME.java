package shared;

public class ConfigWINSOME {
    private String address;
    private int port;
    private String server_rmi_name;
    private int server_rmi_port;

    public ConfigWINSOME(String address, int port, String server_rmi_name, int server_rmi_port){
        this.port = port;
        this.address = address;
        this.server_rmi_name = server_rmi_name;
        this.server_rmi_port = server_rmi_port;
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
}
