package Logic;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import Utilities.DataFile;
import Utilities.DatagramObject;
import Utilities.MediaUtilities;
import Utilities.User;

/**
 * This class is meant to be the object that the server launcher will register for the use
 * of the clients. This class acts as the server itself, which manages all the logic.
 */
public class MediaHandlerServer extends UnicastRemoteObject
        implements MediaHandler, NetworkNode {

    public static final String mediaPath =
            "/home/rdc2/Escritorio/DC/A6/Server/Storage/";

    private List<DataFile> files = new ArrayList<>();

    private static char fileSeparator = '#';

    MediaHandlerServer()
            throws RemoteException {
        // TODO Create a method to extract everything from the database
        files.add(new DataFile(
                "TestingDownload",
                DataFile.Topic.Action,
                "This is a file just for testing downloading purposes.",
                "DefaultUser",
                mediaPath + "testing#download")
        );
    }

    // region Network Node

    @Override
    public DatagramObject join(NetworkNode node) throws RemoteException
    {
        System.out.println("New connection");
        // TODO Add the node to the node list
        NetworkNode newNode = node;

        return new DatagramObject(202, this);
    }

    @Override
    public DatagramObject ping() throws RemoteException
    {
        System.out.println("Received ping");

        return new DatagramObject(200);
    }

    // endregion

    // region Main services

    /**
     * Uploads a file into the server. The file comes encoded into an array of bytes, and ii
     * is also provided a package that contains extra information from the sender.
     *
     * @param encodedFile The encoded file
     * @param information The information provided from the client, which contains the title
     *                    topic, description and the user itself.
     * @return A DatagramObject containing an HTTP status code.
     * @throws IOException Throws this exception if the file cannot be written.
     */
    @Override
    public DatagramObject upload(byte[] encodedFile,
                                 MediaPackage information,
                                 DatagramCertificate certificate)
            throws IOException {
        // TODO Reject upload if the file conflicts

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        int statusCode;
        OutputStream out = null;
        try {
            String path = mediaPath + generateFileName(
                    information.getTitle(),
                    certificate.getUsername());

            // Convert the bytes into a file and add it to the folder
            out = new BufferedOutputStream(new FileOutputStream(path));
            out.write(encodedFile);

            // Add a new logical file
            addDataFile(information, certificate.getUsername());
        } finally {
            if (out != null) {
                out.close();
            }
            // Status code: accepted
            statusCode = 201;

            // Notify
            notifySubscribers(information.getTopic(), information.getTitle());
        }

        return new DatagramObject(statusCode);
    }

    /**
     * Downloads a file from the server. The file is encoded in a byte array.
     * The file is obtained by a title. The array will be empty if the title cannot be found.
     *
     * @param title The title of the file that is meant to be downloaded.
     * @return A DatagramObject containing an HTTP status code and byte array encoding
     * the file.
     * @throws IOException Throws this exception if the file cannot be downloaded.
     */
    @Override
    public DatagramObject download(String title,
                                   DatagramCertificate certificate)
            throws IOException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        DataFile file = getFileByTitle(title);

        if (file != null) {
            System.out.println("Client download file with title [" + title + "]");
            return new DatagramObject(202,
                    MediaUtilities.convertToByes(file.getPath()));
        } else {
            System.out.println("Requested file not found.");
            return new DatagramObject(404);
        }
    }

    /**
     * Edits the file with the target title with the information sent in the package.
     *
     * @param title       The title of the file to be edited.
     * @param information A package which contains required extra information for
     *                    the operation.
     * @param certificate The user certificate to validate the operation.
     * @return
     * @throws RemoteException
     */
    @Override
    public DatagramObject edit(String title,
                               MediaPackage information,
                               DatagramCertificate certificate)
            throws RemoteException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        // Find the file
        for (DataFile file : files) {
            if (file.getTitle().equals(title)) {
                // Check if the user is the owner and thus has permission
                if (file.getOwner().equals(certificate.getUsername())) {
                    // Overwrite logical file
                    files.remove(file);
                    addDataFile(information, certificate.getUsername());
                    // TODO Change physical file name
                    // Success, No content
                    return new DatagramObject(204);
                } else {
                    // Operation unauthorized
                    return new DatagramObject(401);
                }
            }
        }
        // Not found
        return new DatagramObject(404);
    }

    /**
     * Deletes the file with the target title.
     *
     * @param title       The title of the file to be deleted.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code.
     * @throws RemoteException Throws this exception if there is any connection problem.
     */
    @Override
    public DatagramObject delete(String title,
                                 DatagramCertificate certificate)
            throws RemoteException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        // Find the file
        for (DataFile file : files) {
            if (file.getTitle().equals(title)) {
                // Check if the user is the owner and thus has permission
                if (file.getOwner().equals(certificate.getUsername())) {
                    // Remove both logical and physical file
                    files.remove(file);
                    (new File(file.getPath())).delete();
                    // Success, No content
                    return new DatagramObject(204);
                } else {
                    // Operation unauthorized
                    return new DatagramObject(401);
                }
            }
        }
        // Not found
        return new DatagramObject(404);
    }

    // endregion

    // region Subscription

    /**
     * Subscribes the user to an specific Topic. The user will be notified
     * any time a new file with this Topic is uploaded.
     *
     * @param topic  The Topic which the user subscribes.
     * @param caller The object used for the callback.
     * @return A value corresponding to a HTTP status.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    @Override
    public DatagramObject subscribe(
            DataFile.Topic topic,
            MediaCallback caller,
            DatagramCertificate certificate)
            throws RemoteException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        // Add the client callback if it does not exist yet
        if (!clientCallback.contains(caller)) {
            clientCallback.add(caller);
        }
        return SubscriptionHandler.handler.addSubscriber(certificate.getUsername(), topic) ?
                new DatagramObject(201) :
                new DatagramObject(409);
    }

    @Override
    public DatagramObject unsubscribe(
            DataFile.Topic topic,
            DatagramCertificate certificate)
            throws RemoteException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        // TODO Remove the callback if the client has not any subscription

        return SubscriptionHandler.handler.removeSubscriber(
                certificate.getUsername(), topic) ?
                new DatagramObject(201) : new DatagramObject(409);
    }

    private List<MediaCallback> clientCallback = new ArrayList<>();

    private void notifySubscribers(DataFile.Topic topic, String title) {
        ArrayList<String> subsList = SubscriptionHandler.handler.getSubscriptionList(topic);

        for (MediaCallback callback : clientCallback) {
            try {
                callback.notifySubscriber(
                        "\t-New file of [" + topic + "] with title uploaded: " + title);
            } catch (RemoteException e) {
                System.out.println("Client could not be notified.");
            }
        }
    }

    // endregion

    // region Queries

    /**
     * Returns a list of titles that contain the text passed by parameter in the description
     * or in the title.
     *
     * @param text The text for do the search.
     * @return A list of titles.
     */
    @Override
    public DatagramObject getContents(
            String text,
            DatagramCertificate certificate)
            throws RemoteException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        // Create the filtered list
        List<String> filteredFiles = new ArrayList<>();
        for (DataFile dataFile : files) {
            if (dataFile.getTitle().contains(text) ||
                    dataFile.getDescription().contains(text)) {
                filteredFiles.add(dataFile.getTitle());
            }
        }

        return new DatagramObject(201, filteredFiles);
    }

    /**
     * Returns a list of titles which the topic is the one specified in the parameter.
     *
     * @param topic The topic for do the search.
     * @return A list of titles.
     */
    @Override
    public DatagramObject getContents(
            DataFile.Topic topic,
            DatagramCertificate certificate)
            throws RemoteException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        List<String> filteredFiles = new ArrayList<>();
        for (DataFile dataFile : files) {
            if (dataFile.getTopic() == topic) {
                filteredFiles.add(dataFile.getTitle());
            }
        }

        return new DatagramObject(201, filteredFiles);
    }

    // endregion

    // region Media handler tools

    /**
     * Creates an unique name for a file from the title and the user who creates it.
     *
     * @param title    The title of the file
     * @param username The user name of the user who uploads the file.
     * @return A name generated by the hashes of the user name and the title.
     */
    private String generateFileName(String title, String username) {
        return username + fileSeparator + title;
    }

    /**
     * Finds and returns a file which title is the one passed by parameter.
     *
     * @param title The title of the file.
     * @return The DataFile corresponding to the title.
     */
    private DataFile getFileByTitle(String title) {
        for (DataFile file : files) {
            if (file.getTitle().equals(title)) {
                System.out.println("File with title[" + title + "] found.");
                return file;
            }
        }
        System.out.println("File with title[" + title + "] not found.");
        return null;
    }

    /**
     * Adds a new DataFile reference.
     *
     * @param title       The title of the file.
     * @param topic       The topic of the file.
     * @param description The description of the file.
     * @param username    The username of the Utilities.User who uploads the file.
     */
    public void addDataFile(String title, DataFile.Topic topic, String description, String username) {
        String filePath = mediaPath + username.hashCode();
        files.add(new DataFile(
                title,
                topic,
                description,
                filePath));
    }

    /**
     * Adds a new DataFile reference.
     *
     * @param information A MediaPackage structure that stores all the information required
     *                    for a file transfer.
     */
    private void addDataFile(MediaPackage information, String owner) {
        String filePath = mediaPath + owner;
        files.add(new DataFile(
                information.getTitle(),
                information.getTopic(),
                information.getDescription(),
                filePath));
    }

    // endregion

    // region Login

    /**
     * Tries to log in in the server. If the user and password are correct and
     * registered, the server will return an HTTP success code and a value
     * to keep the session, otherwise will only return an HTTP client error.
     *
     * @param user The user information containing the name and the password.
     * @return An HTTP status code and a value if the login was successful.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    @Override
    public DatagramObject login(User user) throws RemoteException {
        User localUser = ServerLoginHandler.getUserFromDB(user.getUsername());

        // User does not exist with such username
        if (localUser == null) {
            // Unprocessable entity
            return new DatagramObject(422);
        }

        // User exists, password is correct
        if (localUser.authenticate(user.getPassword())) {
            // Add a registry for this new active user
            int certificate = ServerLoginHandler.generateCertificate();
            user.setDigitalCertificate(certificate);
            ServerLoginHandler.addActiveUser(user);
            // Accepted
            return new DatagramObject(200,
                    new DatagramCertificate(
                            user.getUsername(),
                            certificate
                    ));
        }
        // User exists, password is wrong
        else {
            // Unprocessable entity
            return new DatagramObject(401);
        }
    }

    // endregion
}
