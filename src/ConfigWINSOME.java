public class ConfigWINSOME {
    private String address;
    private int port;

    public ConfigWINSOME(String address, int port){
        this.port = port;
        this.address = address;
    }

    public ConfigWINSOME(){

    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
