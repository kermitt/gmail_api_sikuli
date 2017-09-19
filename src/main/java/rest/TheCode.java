package rest;

public class TheCode {

    private final long id;// the ith result
    private final String content; // some activation code such as 076612

    public TheCode(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}