package shared;

public class ConfigWINSOME {
    private String address;
    private int port;
    private String server_rmi_name;

    public ConfigWINSOME(String address, int port, String server_rmi_name){
        this.port = port;
        this.address = address;
        this.server_rmi_name = server_rmi_name;
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
}
