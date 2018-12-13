package Logic;

import Client.MediaCallback;

public class Subscriber {

    private final String username;
    private MediaCallback callback;

    public Subscriber(String username, MediaCallback callback) {
        this.username = username;
        this.callback = callback;
    }

    // region Getters

    public String getUsername() {
        return username;
    }

    public MediaCallback getCallback() {
        return callback;
    }

    // endregion

    // region Setters

    public void setCallback(MediaCallback callback) {
        this.callback = callback;
    }

    // endregion
}
