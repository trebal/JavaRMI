package Logic;

import Utilities.DataFile;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MediaHandler extends Remote {

    /**
     * Uploads a file to the server. The file is encoded in a byte array.
     * A MediaPackage is also provided containing the extra information required for the
     * operation: title, topic, description, etc.
     *
     * @param encodedFile A byte array encoding the original file.
     * @param information A package which contains required extra information for the operation.
     * @return A value corresponding to a HTTP status.
     * @throws IOException Throws this exception if the file cannot be uploaded.
     */
    int upload(byte[] encodedFile, MediaPackage information) throws IOException;

    /**
     * Downloads a file from the server. The file is encoded in a byte array.
     * The file is obtained by a title. The array will be empty if the title cannot be found.
     *
     * @param title The title of the file that is meant to be downloaded.
     * @return A byte array encoding the original file.
     * @throws IOException Throws this exception if the file cannot be downloaded.
     */
    byte[] download(String title) throws IOException;

    /**
     * Returns a list of titles which its file contains the text specified by
     * the parameter.
     * @param text The text to us as keyword.
     * @return A list of titles.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    List<String> getContents(String text) throws RemoteException;

    /**
     * Returns a list of titles which its topic is the one specified by the
     * parameter.
     * @param topic The Topic to us as keyword.
     * @return A list of titles.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    List<String> getContents(DataFile.Topic topic) throws RemoteException;

    /**
     * Subscribes the user to an specific Topic. The user will be notified
     * any time a new file with this Topic is uploaded.
     * @param topic The Topic which the user subscribes.
     * @param caller The object used for the callback.
     * @param username The username of the User who subscribes.
     * @return A value corresponding to a HTTP status.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    int subscribe(DataFile.Topic topic, MediaCallback caller, String username) throws RemoteException;

    /**
     * Unsubscribes the user from an specific Topic.
     * @param topic The Topic which the user unsubscribes.
     * @param username The username of the User who subscribes.
     * @return A value corresponding to a HTTP status.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    int unsubscribe(DataFile.Topic topic, String username) throws RemoteException;
}
