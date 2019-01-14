package Logic;

import java.io.Serializable;

/**
 * A class to represent logical files. A logical file is an object which
 * contains a title, a topic and a description of the file. Also, it has
 * the field path, which points to the physical file, stored in the server.
 */
public class DataFile implements Serializable {

    /**
     * The title of the file.
     */
    private final String title;
    /**
     * The topic of the file (enum to have more control and a discrete
     * value.
     */
    private final Topic topic;
    /**
     * The description of the file.
     */
    private final String description;
    /**
     * The owner of this file.
     */
    private final String owner;
    /**
     * The path where the physical file is located.
     */
    private String path;

    // TODO Use dictionary instead
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

    /**
     * The constructor of the class.
     * @param title The title of the file.
     * @param topic The topic of the file.
     * @param description The description of the file.
     * @param owner The owner of the file.
     * @param path The path where of the physical file.
     */
    public DataFile(String title, Topic topic, String description, String owner,
                    String path) {

        this.title = title;
        this.topic = topic;
        this.description = description;
        this.owner = owner;
        this.path = path;
    }

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

}
