import java.util.ArrayList;
import java.util.Date;

public class Post {
    private final int id;
    private final String author;
    private final String content;
    private final Date timestamp;
    private final int vote;
    private final ArrayList<Comment> comments;

    public Post(int id, String author, String content){
        this.id = id;
        this.author = author;
        this.content = content;
        this.timestamp = new Date();
        this.vote = 0;
        this.comments = new ArrayList<>();
    }
}
