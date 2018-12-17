package Client;

import Logic.*;
import Server.MediaHandler;
import Utilities.MediaUtilities;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// TODO Show list to delete
public class MediaHandlerClient {

    private Boolean running = true;
    private static final String DEFAULT_DIRECTORY =
            "user.home";

    private MediaHandler mediaHandler;
    private MediaCallback callback;
    private DatagramCertificate certificate;

    private static BufferedReader br;

    MediaHandlerClient(MediaHandler mediaHandler)
    {
        br = new BufferedReader(new InputStreamReader(System.in));
        this.mediaHandler = mediaHandler;
    }

    // region Functional methods

    /**
     * Handles the execution of the client from the console.
     */
    void run()
    {
        // Log in
        while (true) {
            try {
                System.out.println("Type your user name:");
                String userName = br.readLine();
                System.out.println("Type your password:");
                String userPass = br.readLine();
                User user = new User(userName, userPass);

                DatagramObject status = mediaHandler.login(user);

                if (status.getStatusCode() == 200) {
                    System.out.println("Login successful.");
                    certificate = (DatagramCertificate) status.getContent();
                    callback = new MediaCallbackClient();
                    break;
                } else {
                    System.out.println("Wrong username or password. Try again.");
                }
            } catch (IOException e) {
                System.out.println("Could not log in the server.");
            }
        }

        // Read and handle the commands
        System.out.println("Client ready.");
        while (running) {
            try {
                handleCommand(br.readLine());
            } catch (IOException e) {
                System.out.println("Error while trying to read commands.");;
                break;
            }
        }
    }

    /**
     * Handles the command called by the user.
     *
     * @param commandLine The text line containing the command.
     */
    private void handleCommand(String commandLine) throws IOException {

        StringTokenizer tokenizer = new StringTokenizer(commandLine, " ");
        String command = tokenizer.nextToken();

        switch (command) {
            case "upload":
                upload(
                        mediaHandler,
                        certificate);
                break;

            case "download":
                download(
                        mediaHandler,
                        certificate);
                break;

            case "edit":
                edit(
                        mediaHandler,
                        certificate);
                break;

            case "delete":
                delete(
                        mediaHandler,
                        certificate);
                break;

            case "subscribe":
                subscribe(
                        mediaHandler,
                        callback,
                        certificate);
                break;

            case "unsubscribe":
                unsubscribe(
                        mediaHandler,
                        certificate);
                break;

            case "get":
                get(
                        mediaHandler,
                        certificate);
                break;

            case "exit":
                running = false;
                break;

            default:
                System.out.println("Unrecognized command: " + commandLine);
                break;
        }
    }

    // endregion

    // region Media Handler main commands

    /**
     * Handles the upload command, which uploads a file from the server using
     * the passed provided information.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    private static void upload(MediaHandler mediaHandler,
                              DatagramCertificate certificate)
            throws IOException {

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
    private static void download(MediaHandler mediaHandler,
                                DatagramCertificate certificate)
            throws IOException {

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
        } else if (result.getStatusCode() == 500) {
            printStatusMessage(result,
                    "Physical file not found in the server.");
            return;
        }

        // Get the list of files
        ArrayList<DataFile> files = (ArrayList<DataFile>) result.getContent();

        String owner;

        // List contains only one title
        if (files.size() == 1) {
            owner = files.get(0).getOwner();
        }
        // List contains multiple titles, choose by owner
        else {
            int ownerIndex = 1;
            System.out.println("Multiple files found with the same name. " +
                    "Choose from which used you want to download.");
            for (DataFile file : files) {
                System.out.println(
                        ownerIndex + ".-" +
                                file.getTitle() + ", by " + file.getOwner());
                ownerIndex += 1;
            }
            // Get the title and owner, corresponding to the selected index by the user
            int fileIndex = Integer.valueOf(br.readLine()) - 1;
            owner = files.get(fileIndex).getOwner();
            title = files.get(fileIndex).getTitle();
        }
        // Send the request and get the result
        result = mediaHandler.download(title, owner, certificate);

        // Control server errors
        if(result.getStatusCode() >= 500)
        {
            printStatusMessage(result, (String) result.getContent());
            return;
        }

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
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception while writing downloaded file.");
            e.printStackTrace();
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
    private static void edit(MediaHandler mediaHandler,
                            DatagramCertificate certificate)
            throws IOException {

        // Get target file
        System.out.println("Type the title of the file to edit.");
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
        String title;
        DataFile.Topic topic;
        String description;
        System.out.println("Type the new title for the file. Leave empty to not change it.");
        try {
            title = br.readLine();
        } catch (Exception e) {
            title = "";
        }
        System.out.println("Choose a topic for the file. Leave empty to not change it.");
        printTopics();
        try {
            topic = DataFile.Topic.values()[Integer.valueOf(br.readLine()) + 1];
        } catch (Exception e) {
            topic = DataFile.Topic.Undefined;
        }
        System.out.println("Type a description for the file. Leave empty to not change it.");
        try {
            description = br.readLine();
        } catch (Exception e) {
            description = "";
        }
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
        } else {
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
    private static void delete(MediaHandler mediaHandler,
                              DatagramCertificate certificate)
            throws IOException {

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
    private static void subscribe(MediaHandler mediaHandler,
                                 MediaCallback mediaCallback,
                                 DatagramCertificate certificate)
            throws IOException {

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
    private static void unsubscribe(MediaHandler mediaHandler,
                                   DatagramCertificate certificate)
            throws IOException {

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

    /**
     * Handles the get command, which gets lists of titles from the server using the
     * information provided by the user as filter.
     *
     * @param mediaHandler The media handler instance of the server.
     * @param certificate  The user certificate to validate the operation.
     * @throws IOException Throws this exception if any critical error happens.
     */
    private static void get(MediaHandler mediaHandler,
                           DatagramCertificate certificate)
            throws IOException {

        // Get search mode
        System.out.println("Choose a search mode:");
        System.out.println("1. Title");
        System.out.println("2. Topic");
        System.out.println("3. Text");
        int mode;
        while (true) {
            try {
                mode = Integer.valueOf(br.readLine());

                if (mode >= 1 && mode <= 3) {
                    break;
                }
            } catch (Exception e) {
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

    /**
     * Prints the list of topics.
     */
    private static void printTopics() {
        int index = 1;
        for (DataFile.Topic topic : DataFile.Topic.values()) {
            System.out.println(index + ". " + topic);
            index += 1;
        }
    }

    // endregion
}
