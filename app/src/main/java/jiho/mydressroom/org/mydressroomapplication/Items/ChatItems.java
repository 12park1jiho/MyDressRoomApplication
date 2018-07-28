package jiho.mydressroom.org.mydressroomapplication.Items;

public class ChatItems {
    private String name;
    private String title;
    private String text;
    private String uri;

    public ChatItems() {
    }

    public ChatItems(String name, String title, String text, String uri) {
        this.name = name;
        this.title = title;
        this.text = text;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
