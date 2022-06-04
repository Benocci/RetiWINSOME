import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;


public class ClientMainWINSOME {

    public static void main(String[] args) {
        System.out.println("Avvio client in corso...");

        File file;
        if(args.length < 1){
            file = new File("src\\config.json");
            System.out.println("Client avviato con la configurazione di default.");
        }
        else{
            file = new File(args[0]);

            if(!file.exists()){
                file = new File("src\\config.json");
                System.out.println("Client avviato con la configurazione di default.");
            }
            else{
                System.out.println("Client avviato con la configurazione data da \"" + args[0] + "\"");
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ConfigWINSOME config;
        try {
            config = objectMapper.readValue(file, ConfigWINSOME.class);
        }
        catch (Exception e){
            throw new RuntimeException("ERRORE: file di config -> " + e.getMessage());
        }

        System.out.println("Stampo porta=" + config.getPort() + "\nStampo indirizzo=" + config.getAddress());
    }
}
