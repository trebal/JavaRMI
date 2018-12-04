package Logic;

import Utilities.DataFile;

import java.io.Serializable;

// TODO Remove this class and use DataFile instead
public class MediaPackage implements Serializable {

    private final String title;
    private final DataFile.Topic topic;
    private final String description;

    public MediaPackage(String title, DataFile.Topic topic, String description, String username)
    {
        this.title = title;
        this.topic = topic;
        this.description = description;
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


    // endregion
}
