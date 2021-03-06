package Server;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import Client.MediaCallback;
import Logic.DatagramCertificate;
import Logic.MediaPackage;
import Logic.DataFile;
import Logic.DatagramObject;
import Server.Database.UserHandler;
import Utilities.MediaUtilities;
import Logic.User;

/**
 * This class is meant to be the object that the server launcher will register for the use
 * of the clients. This class acts as the server itself, which manages all the logic.
 */
public class MediaHandlerServer extends UnicastRemoteObject
        implements MediaHandler, NetworkNode {

    private List<DataFile> files = new ArrayList<>();

    private static char fileSeparator = '#';
    private static String MEDIA_PATH;

    MediaHandlerServer(String mediaPath)
            throws RemoteException {

        MEDIA_PATH = mediaPath;

        // TODO Create a method to extract everything from the database
        files.add(new DataFile(
                "TestingDownload",
                DataFile.Topic.Action,
                "This is a file just for testing downloading.",
                "admin",
                MEDIA_PATH + "Admin#TestingDownload")
        );

        files.add(new DataFile(
                "TestingDownload",
                DataFile.Topic.Action,
                "This is a file just for testing downloading.",
                "1",
                MEDIA_PATH + "1#TestingDownload")
        );

        files.add(new DataFile(
                "TestingConflict",
                DataFile.Topic.Action,
                "This is a file just for testing upload conflicts.",
                "1",
                MEDIA_PATH + "Admin#TestingConflict")
        );

        files.add(new DataFile(
                "TestingDelete",
                DataFile.Topic.Action,
                "This is a file just for testing deleting purposes.",
                "1",
                MEDIA_PATH + "Admin#TestingDelete")
        );
    }

    // region Network Node

    @Override
    public DatagramObject join(NetworkNode node) throws RemoteException {
        System.out.println("New connection");
        // TODO Add the node to the node list
        NetworkNode newNode = node;

        return new DatagramObject(202, this);
    }

    @Override
    public DatagramObject ping() throws RemoteException {
        System.out.println("Received ping");

        return new DatagramObject(200);
    }

    // endregion

    // region Media handler services

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

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        // Generate the path
        String path = MEDIA_PATH + generateFileName(
                information.getTitle(), certificate.getUsername());

        int statusCode;
        OutputStream out = null;
        try {
            // Convert the bytes into a file and add it to the folder
            out = new BufferedOutputStream(new FileOutputStream(path));
            out.write(encodedFile);

            // Add a new logical file
            addDataFile(information, certificate.getUsername());

            // Accepted
            statusCode = 201;

            // Notify subscribers
            notifySubscribers(information.getTopic(), information.getTitle());
        } catch (Exception e) {
            // Internal server error
            System.out.println("Internal server error while using [upload]");
            return new DatagramObject(500,
                    "Server could not write file.");
        } finally {
            if (out != null) {
                out.close();
            }
        }



        // Notify the WS
        try{
            WebServiceHandler.postContent(
                    new DataFile(
                            information.getTitle(),
                            information.getTopic(),
                            information.getDescription(),
                            certificate.getUsername(),
                            ""));
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                                   String owner,
                                   DatagramCertificate certificate)
            throws IOException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        // Get the data file
        DataFile file = getExactFile(title, owner);

        // Not found
        if (file == null) {
            return new DatagramObject(404);
        }

        // Success
        try {
            byte[] content = MediaUtilities.convertToByes(file.getPath());
            return new DatagramObject(202,
                    content);
        }
        // Internal server error (physical file not found)
        catch (Exception e) {
            System.out.println("Internal server error while using [download]");
            return new DatagramObject(500,
                    "Physical file could not be found in the server.");
        }
    }

    /**
     * Edits the file with the target title with the information sent in the package.
     *
     * @param title       The title of the file to be edited.
     * @param information A package which contains required extra information for
     *                    the operation.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code.
     * @throws RemoteException Throws this exception if there is any connection problem.
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

        // Get the data file
        DataFile file = getExactFile(title, certificate.getUsername());

        // Not found
        if (file == null) {
            return new DatagramObject(404);
        }

        // Overwrite logical file
        files.remove(file);
        addDataFile(information, certificate.getUsername());
        // Rename physical file
        File editFile = new File(file.getPath());
        File newFile = new File(MEDIA_PATH + generateFileName(
                information.getTitle(),
                certificate.getUsername()));
        if (editFile.renameTo(newFile)) {
            // Notify the WS
            try {
                WebServiceHandler.putContent(
                        title,
                        certificate.getUsername(),
                        new DataFile(
                                information.getTitle(),
                                information.getTopic(),
                                information.getDescription(),
                                certificate.getUsername(),
                                ""));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Success, No content
            return new DatagramObject(204);
        } else {
            // Server error, Internal: file not found in the directory
            System.out.println("Internal server error while using [edit]");
            return new DatagramObject(500,
                    "Physical file could not be found in the server.");
        }
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

        // Get the file
        DataFile file = getExactFile(title, certificate.getUsername());

        // File not found
        if (file == null) {
            return new DatagramObject(404);
        }

        // Remove logical file
        files.remove(file);
        // Remove physical file
        File rmFile = new File(file.getPath());
        if (rmFile.delete()) {
            // Notify the WS
            try{
                WebServiceHandler.deleteContent(
                        new DataFile(
                                file.getTitle(),
                                file.getTopic(),
                                file.getDescription(),
                                certificate.getUsername(),
                                ""));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Success, No content
            return new DatagramObject(204);
        } else {
            // Server error, Internal: file not found in the directory
            System.out.println("Internal server error while using [delete]");
            System.out.println(file.getPath());
            return new DatagramObject(500,
                    "Physical file could not be found in the server.");
        }
    }

    // endregion

    // region Subscription

    private List<MediaCallback> clientCallback = new ArrayList<>();

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

        return SubscriptionHandler.handler.addSubscriber(
                certificate.getUsername(), topic, caller) ?
                new DatagramObject(201) :
                new DatagramObject(409);
    }

    /**
     * Unsubscribes the user from an specific Topic.
     *
     * @param topic       The Topic which the user unsubscribes.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    @Override
    public DatagramObject unsubscribe(
            DataFile.Topic topic,
            DatagramCertificate certificate)
            throws RemoteException {

        // Validate user certificate
        if (!ServerLoginHandler.validateCertificate(certificate)) {
            return new DatagramObject(401);
        }

        return SubscriptionHandler.handler.removeSubscriber(
                certificate.getUsername(), topic) ?
                new DatagramObject(201) :
                new DatagramObject(409);
    }

    /**
     * Notifies all subscribers about a new file submission belonging to the topic .
     *
     * @param topic The Topic of the new uploaded file.
     * @param title The title of the new uploaded file.
     */
    private void notifySubscribers(DataFile.Topic topic, String title) {

        SubscriptionHandler.handler.notifySubscribers(topic, title);

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

    /**
     * Returns the DataFile corresponding to the physical file with the passed title
     * and owner.
     *
     * @param title       The title of the file.
     * @param owner       The owner of the file.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code and a titles (if found).
     * @throws RemoteException Throws this exception if there is any problem.
     */
    @Override
    public DatagramObject getFile(String title,
                                  String owner,
                                  DatagramCertificate certificate)
            throws RemoteException {
        for (DataFile file : files) {
            if (file.getTitle().equals(title) && file.getOwner().equals(owner)) {
                return new DatagramObject(200, file);
            }
        }

        return new DatagramObject(404);
    }

    /**
     * Returns a DataFile list containing each one the information of a physical file
     * with the passed title.
     *
     * @param title       The title of the file.
     * @param certificate The user certificate to validate the operation.
     * @return A DatagramObject containing an HTTP status code and a list of titles.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    @Override
    public DatagramObject getFilesByTitle(String title,
                                          DatagramCertificate certificate)
            throws RemoteException {
        ArrayList<DataFile> coincidences = new ArrayList<>();

        for (DataFile file : files) {
            if (file.getTitle().contains(title)) {
                coincidences.add(file);
            }
        }

        return (!coincidences.isEmpty()) ?
                new DatagramObject(200, coincidences) :
                new DatagramObject(404);
    }

    @Override
    public DatagramObject getFilesByOwner(String owner,
                                          DatagramCertificate certificate)
        throws RemoteException{
        ArrayList<DataFile> coincidences = new ArrayList<>();

        for (DataFile file : files) {
            if (file.getOwner().equals(owner)) {
                coincidences.add(file);
            }
        }

        return (!coincidences.isEmpty()) ?
                new DatagramObject(200, coincidences) :
                new DatagramObject(404);
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
     * Finds and returns a file which title and owner are the ones passed by parameter.
     *
     * @param title The title of the file.
     * @return The DataFile corresponding to the title.
     */
    private DataFile getExactFile(String title, String owner) {

        for (DataFile file : files) {
            if (file.getTitle().equals(title) && file.getOwner().equals(owner)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Adds a new logical file in the server.
     *
     * @param information A MediaPackage structure that stores all the information required
     *                    for a file transfer.
     * @param owner       The owner of the file.
     */
    private void addDataFile(MediaPackage information, String owner) {
        String filePath = MEDIA_PATH + generateFileName(information.getTitle(), owner);
        files.add(new DataFile(
                information.getTitle(),
                information.getTopic(),
                information.getDescription(),
                owner,
                filePath));
    }

    // endregion

    // region Login

    /**
     * Tries to log in in the server. If the user and password are correct and
     * registered, the server will return an HTTP success code and a certificate
     * to keep the session, otherwise will only return an HTTP client error.
     *
     * @param user The user information containing the name and the password.
     * @return An HTTP status code and a certificate if the login was successful.
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
            // Unauthorized entity
            return new DatagramObject(401);
        }
    }

    // endregion
}
