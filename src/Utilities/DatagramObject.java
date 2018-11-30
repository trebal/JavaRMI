package Utilities;

import java.io.Serializable;

/**
 * An object meant to be used as a mean of status code notification.
 */
public class DatagramObject implements Serializable {

    private final int statusCode;
    private final Object content;

    public DatagramObject(int statusCode)
    {
        this.statusCode = statusCode;
        this.content = null;
    }

    public DatagramObject(int statusCode, Object content)
    {
        this.statusCode = statusCode;
        this.content = content;
    }

    // region Getters

    public int getStatusCode()
    {
        return statusCode;
    }

    public Object getContent() {
        return content;
    }

    // endregion
}
