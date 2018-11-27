package Logic;

import Utilities.DataFile;

import java.io.Serializable;

public class MediaPackage implements Serializable {

    private final String title;
    private final DataFile.Topic topic;
    private final String description;
    private final DataFile.Type type;
    private final String username;

    public MediaPackage(String title, DataFile.Topic topic, String description, String username)
    {
        this.title = title;
        this.topic = topic;
        this.description = description;
        this.username = username;
        type = DataFile.Type.Text;
    }

    // region Getters

    public String getTitle() {
        return title;
    }

    public DataFile.Topic getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }

    // endregion
}
