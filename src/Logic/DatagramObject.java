package Logic;

import java.io.Serializable;

/**
 * An object meant to be used as a mean of status code notification
 * and certain information (not always).
 */
public class DatagramObject implements Serializable {

    /**
     * An HTTP status code.
     */
    private final int statusCode;
    /**
     *
     */
    private final Object content;

    /**
     * The constructor used for status notification with no content.
     * @param statusCode The status code of the operation.
     */
    public DatagramObject(int statusCode)
    {
        this.statusCode = statusCode;
        this.content = null;
    }

    /**
     * The constructor used for status notification with extra content.
     * @param statusCode The status code of the operation.
     * @param content The content, which can be anything required or requested.
     */
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
