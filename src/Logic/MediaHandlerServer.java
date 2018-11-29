package Logic;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import Utilities.DataFile;
import Utilities.MediaUtilities;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class is meant to be the object that the server launcher will register for the use
 * of the clients. This class acts as the server itself, which manages all the logic.
 */
public class MediaHandlerServer extends UnicastRemoteObject implements MediaHandler {

    public static final String mediaPath = "/home/rdc2/Escritorio/DC/A6/RMI_Server_Storage/";

    private List<DataFile> files = new ArrayList<>();

    public MediaHandlerServer()
            throws RemoteException {
        // TODO Create a method to extract everything from the database
        files.add(new DataFile(
                "TestingDownload",
                DataFile.Topic.Undefined,
                "This is a file just for testing downloading purposes.",
                mediaPath + "testing#download"));
    }

    // region Main services

    /**
     * Uploads a file into the server. The file comes encoded into an array of bytes, and ii
     * is also provided a package that contains extra information from the sender.
     * @param encodedFile The enconded file
     * @param information The information provided from the client, which contains the title
     *                    topic, description and the user itself.
     * @return An integer which tells the status code resulting from the operation.
     * @throws IOException Throws this exception if the file cannot be written.
     */
    @Override
    public int upload(byte[] encodedFile, MediaPackage information) throws IOException {
        System.out.println("Uploading file in the server");
        int statusCode;
        OutputStream out = null;
        try {
            String path = mediaPath + generateFileName(
                    information.getTitle(),
                    information.getUsername());

            // Convert the bytes into a file and add it to the folder
            out = new BufferedOutputStream(new FileOutputStream(path));
            out.write(encodedFile);

            // Add a new entry for this entity
            addDataFile(information);
        } finally {
            if (out != null) out.close();
            // Status code: accepted
            statusCode = 201;
        }

        return statusCode;
    }

    /**
     * Downloads a file from the server. The file is encoded in a byte array.
     * The file is obtained by a title. The array will be empty if the title cannot be found.
     * @param title The title of the file that is meant to be downloaded.
     * @return A byte array encoding the original file.
     * @throws IOException Throws this exception if the file cannot be downloaded.
     */
    @Override
    public byte[] download(String title) throws IOException {
        System.out.println("File DOWNLOAD request.");

        DataFile file = getFileByTitle(title);

        if(file !=null) {
            System.out.println("Client download file with title [" + title + "]");
            return MediaUtilities.convertToByes(file.getPath());
        }
        else{
            System.out.println("Requested file not found.");
            return null;
        }
    }

    // endregion

    // region Subscription

    /**
     * Subscribes the user to an specific Topic. The user will be notified
     * any time a new file with this Topic is uploaded.
     * @param topic The Topic which the user subscribes.
     * @param caller The object used for the callback.
     * @param username The username of the User who subscribes.
     * @return A value corresponding to a HTTP status.
     * @throws RemoteException Throws this exception if there is any problem.
     */
    @Override
    public int subscribe(DataFile.Topic topic, MediaCallback caller, String username) throws RemoteException {
        caller.notifySubscriber("Hello");
        return SubscriptionHandler.handler.addSubscriber(username, topic) ?
                201 : 409;
    }

    @Override
    public int unsubscribe(DataFile.Topic topic, String username) throws RemoteException
    {
        throw new NotImplementedException();
    }

    private void notifySubscribers(DataFile.Topic topic)
    {
        throw new NotImplementedException();
    }

    // endregion

    // region Queries

    /**
     * Returns a list of titles that contain the text passed by parameter in the description
     * or in the title.
     * @param text The text for do the search.
     * @return A list of titles.
     */
    @Override
    public List<String> getContents(String text) throws RemoteException {
        List<String> filteredFiles = new ArrayList<>();
        for (DataFile dataFile : files) {
            if (dataFile.getTitle().contains(text) ||
                    dataFile.getDescription().contains(text)) {
                filteredFiles.add(dataFile.getTitle());
            }
        }

        return filteredFiles;
    }

    /**
     * Returns a list of titles which the topic is the one specified in the parameter.
     * @param topic The topic for do the search.
     * @return A list of titles.
     */
    @Override
    public List<String> getContents(DataFile.Topic topic) throws RemoteException
    {
        List<String> filteredFiles = new ArrayList<>();
        for (DataFile dataFile : files) {
            if (dataFile.getTopic() == topic) {
                filteredFiles.add(dataFile.getTitle());
            }
        }

        return filteredFiles;
    }

    // endregion

    // region Media handler tools

    /**
     * Creates an unique name for a file from the title and the user who creates it.
     * @param title The title of the file
     * @param username The user name of the user who uploads the file.
     * @return A name generated by the hashes of the user name and the title.
     */
    private String generateFileName(String title, String username)
    {
        return username + "_" + title;
    }

    /**
     * Finds and returns a file which title is the one passed by parameter.
     * @param title The title of the file.
     * @return The DataFile corresponding to the title.
     */
    private DataFile getFileByTitle(String title)
    {
        for(DataFile file : files)
        {
            if(file.getTitle().equals(title))
            {
                System.out.println("File with title[" + title +"] found.");
                return file;
            }
        }
        System.out.println("File with title[" + title +"] not found.");
        return null;
    }

    /**
     * Adds a new DataFile reference.
     * @param title The title of the file.
     * @param topic The topic of the file.
     * @param description The description of the file.
     * @param username The username of the Utilities.User who uploads the file.
     */
    public void addDataFile(String title, DataFile.Topic topic, String description, String username)
    {
        String filePath = mediaPath + username.hashCode();
        files.add(new DataFile(
                title,
                topic,
                description,
                filePath));
    }

    /**
     * Adds a new DataFile reference.
     * @param information A MediaPackage structure that stores all the information required
     *                    for a file transfer.
     */
    private void addDataFile(MediaPackage information) {
        String filePath = mediaPath + information.getUsername().hashCode();
        files.add(new DataFile(
                information.getTitle(),
                information.getTopic(),
                information.getDescription(),
                filePath));
    }

    // TODO Fix and separate this method between the file delete and the reference
    /**
     * Removes a file reference.
     * @param title
     * @param user
     * @return
     */
    public int removeDataFile(String title, String user)
    {
        for(DataFile dataFile : files)
        {
            if(dataFile.getTitle().equals(title))
            {
                // Check if the user is the owner and thus has permission
                if(dataFile.getOwner().equals(user))
                {
                    files.remove(dataFile);
                }
                else{
                    // Operation unauthorized
                    return 401;
                }
            }
        }
        // Not found
        return 403;
    }

    // endregion
}
