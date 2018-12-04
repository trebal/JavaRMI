package Utilities;

public class DataFile {

    private final String title;
    private final Topic topic;
    private final String description;
    private final String owner;
    private String path;

    public enum Topic {
        Action,
        Adventure,
        Comedy,
        Romance,
        Musical,
        Terror,
        Thriller,
        Undefined
    }

    // region Constructors

    public DataFile(String title, Topic topic, String description, String owner) {
        // Mandatory values
        this.title = title;
        this.topic = topic;
        this.description = description;
        this.owner = owner;

        // Default values
        this.path = "";
    }

    public DataFile(String title, Topic topic, String description, String owner,
                    String path) {
        // Mandatory values
        this.title = title;
        this.topic = topic;
        this.description = description;
        this.owner = owner;

        // Default values
        this.path = path;
    }

    // endregion

    // region Getters

    public String getTitle() {
        return title;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        return path;
    }

    public String getOwner() {
        return owner;
    }

    // endregion

    // region Setters

    public void setPath(String path) {
        this.path = path;
    }

    // endregion
}
