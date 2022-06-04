import java.util.Date;

public class Comment {
    private final String author;
    private final String content;
    private final Date timestamp;

    public Comment(String author, String content){
        this.author = author;
        this.content = content;
        this.timestamp = new Date();
    }
}
