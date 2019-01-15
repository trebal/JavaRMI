package Client;

import Logic.DatagramCertificate;
import Server.MediaHandler;
import Logic.MediaPackage;
import Logic.DataFile;
import Logic.DatagramObject;
import Utilities.MediaUtilities;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// TODO Pass all the methods to an interface, and implement it
// TODO Make the buffered reader global
public class MediaHandlerClient {

    private static final String DEFAULT_DIRECTORY =
            "user.home";

    // region Media Handler main commands
    // TODO Show list to delete
    // TODO Allow empty string when editing
    /**
     * Handles the upload command, which uploads a file from the server using
     * the passed provided information.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    public static void upload(MediaHandler mediaHandler,
                              DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Set the file properties
        // TODO Give to choice topic by numeric inputs
        System.out.println("Type a title for the file.");
        String title = br.readLine();
        System.out.println("Type a topic for the file.");
        DataFile.Topic topic = solveTopic(br.readLine());
        System.out.println("Type a description for the file.");
        String description = br.readLine();

        // Check if the file exists in the server
        DatagramObject result = mediaHandler.getFile(title,
                certificate.getUsername(),
                certificate);

        // File exists in the server: try to solve the conflict
        if (result.getStatusCode() == 200) {
            System.out.println("You already have a file with that name in the" +
                    " server. Do you want to overwrite it?");
            System.out.println("1. Yes");
            System.out.println("2. No");

            int commandChoice;
            // Log in
            while (true) {
                try {
                    commandChoice = Integer.valueOf(br.readLine());
                    if (commandChoice == 1) {
                        break;
                    } else if (commandChoice == 2) {
                        System.out.println("Operation canceled.");
                        return;
                    } else {
                        System.out.println("Type 1 for overwrite, 2 for cancel.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String path;

        // Open a file explorer in its default directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(
                new File(System.getProperty(DEFAULT_DIRECTORY)));

        // Return the selected absolute path
        if (fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            System.out.println("Invalid/Null file selected.");
            return;
        }

        // Convert file into bits and create an information package
        byte[] encodedFile = MediaUtilities.convertToByes(path);
        MediaPackage information = new MediaPackage(
                title,
                topic,
                description,
                certificate.getUsername()
        );

        // Send it
        DatagramObject statusCode = mediaHandler.upload(
                encodedFile,
                information,
                certificate);

        printStatusMessage(statusCode,
                "File [" + title + "] uploaded successfully.");
    }

    /**
     * Handles the download command, which downloads a file from the server using
     * the passed title.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    public static void download(MediaHandler mediaHandler,
                                DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Download the file with the title from the server
        System.out.println("Type the title of the file.");
        String title = br.readLine();

        // Get the list of files with that title
        DatagramObject result = mediaHandler.getFilesByTitle(
                title, certificate);

        // File not found
        if (result.getStatusCode() == 404) {
            printStatusMessage(result,
                    "File not found in the server.");
            return;
        }
        else if(result.getStatusCode() == 500)
        {
            printStatusMessage(result,
                    "Physical file not found in the server.");
            return;
        }

        // Get the list of files
        ArrayList<DataFile> files = (ArrayList<DataFile>)result.getContent();

        String owner;

        // List contains only one title
        if(files.size() == 1)
        {
            owner = files.get(0).getOwner();
        }
        // List contains multiple titles, choose by owner
        else{
            int ownerIndex = 1;
            System.out.println("Multiple files found with the same name." +
                    "Choose from which used you want to download.");
            for(DataFile file : files)
            {
                System.out.println(
                        ownerIndex + ".-" +
                        file.getTitle() +", by " + file.getOwner());
                ownerIndex+=1;
            }
            // Get the owner, corresponding to the selected index by the user
            try {
                owner = files.get(Integer.valueOf(br.readLine()) - 1).getOwner();
            }
            catch (Exception e)
            {
                System.out.println("Owner index not correct. Operation cancelled.");
                return;
            }
        }
        result = mediaHandler.download(title, owner, certificate);

        // Open a file explorer and set its default directory
        String path;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(
                new File(System.getProperty(DEFAULT_DIRECTORY)));

        // Return the selected absolute path
        if (fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            System.out.println("Invalid/Null file selected.");
            return;
        }

        // Write the file
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(path));
            out.write((byte[]) result.getContent());
        } finally {
            if (out != null) out.close();
        }

        printStatusMessage(result,
                "File [" + title + "] downloaded successfully.");
    }

    /**
     * Handles the edit command, which edits a file from the server using
     * the passed title.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    public static void edit(MediaHandler mediaHandler,
                            DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Get owner files
        DatagramObject queryResult = mediaHandler.getFilesByOwner(certificate.getUsername(), certificate);

        // Not a single file found of this user
        if(queryResult.getStatusCode() == 404)
        {
            System.out.println("You do not own any file and so you cannot edit anything. " +
                    "Operation cancelled.");
            return;
        }
        // List files
        else{
            System.out.println("You can edit the following files.");
            ArrayList<DataFile> foundFiles = (ArrayList<DataFile>) queryResult.getContent();
            int fileIndex = 1;
            for(DataFile file : foundFiles)
            {
                System.out.println(
                        fileIndex + ".-" + file.getTitle());
                fileIndex+=1;
            }
        }

        // Get target file
        System.out.println("Choose a file to edit.");
        String targetTitle = br.readLine();

        // Check if the file exists
        DatagramObject result = mediaHandler.getFile(
                targetTitle,
                certificate.getUsername(),
                certificate);

        // If the file does not exist, finish transaction
        if (result.getStatusCode() == 404) {
            printStatusMessage(result.getStatusCode(),
                    "You do not own any file with title [" + targetTitle + "]");
            return;
        }

        // Get file properties
        System.out.println("Type the new title for the file. Leave empty to not change it.");
        String title = br.readLine();
        System.out.println("Type a topic for the file. Leave empty to not change it.");
        DataFile.Topic topic = solveTopic(br.readLine());
        System.out.println("Type a description for the file. Leave empty to not change it.");
        String description = br.readLine();

        MediaPackage information = new MediaPackage(
                title,
                topic,
                description,
                certificate.getUsername()
        );

        // Send
        result = mediaHandler.edit(
                targetTitle,
                information,
                certificate);

        if (result.getStatusCode() >= 200 && result.getStatusCode() < 300) {
            printStatusMessage(result,
                    "File with with title [" + targetTitle + "] edited successfully");
        }
        else
        {
            printStatusMessage(result,
                    (String) result.getContent());
        }
    }

    /**
     * Handles the delete command, which deletes a file from the server using
     * the passed title.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    public static void delete(MediaHandler mediaHandler,
                              DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Get target file
        System.out.println("Type the title of the file to delete.");
        String targetTitle = br.readLine();

        // Send
        DatagramObject result = mediaHandler.delete(targetTitle, certificate);

        if (result.getStatusCode() >= 400 && result.getStatusCode() < 500) {
            printStatusMessage(result.getStatusCode(),
                    "You do not own any file with title [" + targetTitle + "]");
        } else if (result.getStatusCode() >= 200 && result.getStatusCode() < 300) {
            printStatusMessage(result,
                    "File with with title [" + targetTitle + "] deleted successfully.");
        } else if (result.getStatusCode() >= 500) {
            printStatusMessage(result.getStatusCode(),
                    "Internal server error: " + (String) result.getContent());
        }
    }

    // endregion

    // region Media Handler subscription

    /**
     * Handles the subscribe command, which subscribes the user to a topic.
     *
     * @param mediaHandler  The media handler instance of the server.
     * @param mediaCallback The media callback instance used by the server.
     * @param certificate   The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    public static void subscribe(MediaHandler mediaHandler,
                                 MediaCallback mediaCallback,
                                 DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Read and parse the topic
        System.out.println("Type the topic you want to subscribe.");
        DataFile.Topic topic = solveTopic(br.readLine());

        // Send
        DatagramObject result = mediaHandler.subscribe(
                topic,
                mediaCallback,
                certificate);

        if (result.getStatusCode() == 201) {
            printStatusMessage(result.getStatusCode(),
                    "Subscribed to [" + topic + "] successfully.");
        } else {
            printStatusMessage(result.getStatusCode(),
                    "Cannot subscribe to [" + topic + "] because you are" +
                            "already subscribed.");
        }
    }

    /**
     * Handles the unsubscribe command, which unsubscribes the user to a topic.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    public static void unsubscribe(MediaHandler mediaHandler,
                                   DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Read and parse the topic
        System.out.println("Type the topic you want to unsubscribe.");
        DataFile.Topic topic = solveTopic(br.readLine());

        // Send
        DatagramObject result = mediaHandler.unsubscribe(
                topic,
                certificate);

        if (result.getStatusCode() == 201) {
            printStatusMessage(result.getStatusCode(),
                    "Unsubscribed to [" + topic + "] successfully.");
        } else {
            printStatusMessage(result.getStatusCode(),
                    "Cannot unsubscribe to [" + topic + "] because you are" +
                            "not subscribed.");
        }
    }

    // endregion

    // region Media Handler queries

    // TODO Handle string before parsing to int to avoid errors

    /**
     * Handles the get command, which gets lists of titles from the server using the
     * information provided by the user as filter.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    public static void get(MediaHandler mediaHandler,
                           DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Get search mode
        System.out.println("Choose a search mode:");
        System.out.println("1. Title");
        System.out.println("2. Topic");
        System.out.println("3. Text");
        int mode;
        while(true)
        {
            try{
                mode  = Integer.valueOf(br.readLine());

                if(mode >= 1 && mode <= 3)
                {
                    break;
                }
            }
            catch (Exception e)
            {
                System.out.println("Not a valid number. " +
                        "Choose again a value between 1 and 3 (included):");
            }
        }

        switch (mode) {
            // By title
            case 1: {
                System.out.println("Type the title:");
                String title = br.readLine();
                DatagramObject result = mediaHandler.getFilesByTitle(title, certificate);

                if (result.getStatusCode() == 404) {
                    printStatusMessage(result.getStatusCode(),
                            "No files have been found with title [" + title + "]");
                    return;
                }

                printStatusMessage(result.getStatusCode(),
                        "Search by title result: ");
                ArrayList<DataFile> resultList = (ArrayList<DataFile>) result.getContent();
                for (DataFile file : resultList) {
                    System.out.println("\t-" + file.getTitle() +
                            ", by " + file.getOwner());
                }

                break;
            }

            // By topic
            case 2: {
                System.out.println("Type the topic:");
                DataFile.Topic topic = solveTopic(br.readLine());
                DatagramObject result = mediaHandler.getContents(topic, certificate);

                if (result.getStatusCode() == 404) {
                    printStatusMessage(result.getStatusCode(),
                            "No files have been found with topic [" + topic + "]");
                    return;
                }

                printStatusMessage(result.getStatusCode(),
                        "Search by topic result: ");
                List<String> resultList = (List<String>) result.getContent();
                for (String title : resultList) {
                    System.out.println("\t-" + title);
                }

                break;
            }

            // By description
            default: {
                System.out.println("Type the text:");
                String text = br.readLine();
                DatagramObject result = mediaHandler.getContents(text, certificate);

                if (result.getStatusCode() == 404) {
                    printStatusMessage(result.getStatusCode(),
                            "No files have been found with text [" + text + "]");
                    return;
                }

                printStatusMessage(result.getStatusCode(),
                        "Search by text result: ");
                List<String> resultList = (List<String>) result.getContent();
                for (String title : resultList) {
                    System.out.println("\t-" + title);
                }
            }
        }
    }

    // endregion

    // region Utilities

    private static void printStatusMessage(int status, String message) {
        System.out.println("[" + status + "]: " + message);
    }

    private static void printStatusMessage(DatagramObject statusObject, String message) {
        System.out.println("[" + statusObject.getStatusCode() + "]: " + message);
    }

    /**
     * Converts an String to a Topic. If the String matches the Topic, it will return the
     * corresponding topic, otherwise it will return the default value: Undefined.
     *
     * @param topic The String corresponding to the Topic name.
     * @return The Topic enum type corresponding to the String.
     */
    private static DataFile.Topic solveTopic(String topic) {
        // Normalize the topic to match the enum syntax.
        topic = topic.toLowerCase();
        topic = topic.substring(0, 1).toUpperCase() + topic.substring(1, topic.length());

        DataFile.Topic topicValue;

        // Try to convert it to the corresponding enum topic, and if it is not possible,
        // use the default value Undefined.
        try {
            topicValue = DataFile.Topic.valueOf(topic);
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Non recognized topic [" + topic + "]: using default [Undefined].");
            topicValue = DataFile.Topic.Undefined;
        }

        return topicValue;
    }

    // endregion
}
