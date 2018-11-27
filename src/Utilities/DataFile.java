package Utilities;

import Logic.MediaHandlerImpl;
import com.sun.deploy.util.StringUtils;

import javax.swing.*;
import javax.swing.text.Utilities;

public class DataFile {

    private final String title;
    private final Topic topic;
    private final String description;
    private final Type type;
    private final String owner;
    private String path;

    public enum Type {
        Text,
        Audio,
        Video
    }

    public enum Topic {
        Action,
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
        this.path = MediaHandlerImpl.mediaPath;
        type = Type.Text;
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
        type = Type.Text;
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
