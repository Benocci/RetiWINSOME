import java.util.Date;
import java.util.Set;

public class User {
    private final String username;
    private final String password;
    private final Set<String> tags;
    private final Date timestamp;

    public User(String username, String password, Set<String> tags){
        this.username = username;
        this.password = password;
        this.tags = tags;
        this.timestamp = new Date();
    }
}
