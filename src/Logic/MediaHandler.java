package Logic;

import Utilities.DataFile;
import Utilities.DatagramObject;
import Utilities.User;

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
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code.
     * @throws IOException Throws this exception if the file cannot be uploaded.
     */
    DatagramObject upload(byte[] encodedFile,
                          MediaPackage information,
                          DatagramCertificate certificate)
            throws IOException;

    /**
     * Downloads a file from the server. The file is encoded in a byte array.
     * The file is obtained by a title. The array will be empty if the title cannot be found.
     *
     * @param title       The title of the file that is meant to be downloaded.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code and byte array encoding
     * the original file.
     * @throws IOException Throws this exception if the file cannot be downloaded.
     */

    DatagramObject download(String title,
                            DatagramCertificate certificate)
            throws IOException;

    /**
     * Returns a list of titles which its file contains the text specified by
     * the parameter.
     *
     * @param text The text to us as keyword.
     * @return A DatagramObject containing an HTTP status code and a list of titles.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    DatagramObject getContents(String text,
                               DatagramCertificate certificate)
            throws RemoteException;

    /**
     * Returns a list of titles which its topic is the one specified by the
     * parameter.
     *
     * @param topic       The Topic to us as keyword.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code and a list of titles.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    DatagramObject getContents(DataFile.Topic topic,
                               DatagramCertificate certificate)
            throws RemoteException;

    /**
     * Subscribes the user to an specific Topic. The user will be notified
     * any time a new file with this Topic is uploaded.
     *
     * @param topic       The Topic which the user subscribes.
     * @param caller      The object used for the callback.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    DatagramObject subscribe(DataFile.Topic topic,
                             MediaCallback caller,
                             DatagramCertificate certificate)
            throws RemoteException;

    /**
     * Unsubscribes the user from an specific Topic.
     *
     * @param topic       The Topic which the user unsubscribes.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    DatagramObject unsubscribe(DataFile.Topic topic,
                               DatagramCertificate certificate)
            throws RemoteException;

    /**
     * Tries to log in in the server. If the user and password are correct and
     * registered, the server will return an HTTP success code and a value
     * to keep the session, otherwise will only return an HTTP client error.
     *
     * @param user The user information containing the name and the password.
     * @return A DatagramObject containing an HTTP status code and the certificate.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    DatagramObject login(User user) throws RemoteException;
}
