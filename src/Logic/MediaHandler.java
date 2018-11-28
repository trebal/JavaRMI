package Logic;

import Utilities.DataFile;

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

    List<String> getContents(String text) throws RemoteException;

    List<String> getContents(DataFile.Topic topic) throws RemoteException;

    int subscribe(DataFile.Topic topic, String user) throws RemoteException;

    int unsubscribe(DataFile.Topic topic, String user) throws RemoteException;
}
