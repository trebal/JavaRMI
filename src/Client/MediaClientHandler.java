package Client;

import Logic.DatagramCertificate;
import Logic.MediaHandler;
import Logic.MediaPackage;
import Utilities.DataFile;
import Utilities.DatagramObject;
import Utilities.MediaUtilities;

import javax.swing.*;
import java.io.*;

// TODO Make this class a singleton, which implements an interface
// TODO Pass all the methods to an interface, and implement it
// TODO Make the buffered reader global
public class MediaClientHandler {

    private static final String DEFAULT_DIRECTORY =
            "user.home";

    // region Media Handler main commands

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
        DataFile.Topic topic = MediaClient.solveTopic(br.readLine());
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
        // TODO Handle different files with same titles but different owners
        String title = br.readLine();
        DatagramObject result = mediaHandler.download(title, certificate);

        // File not found
        if (result.getStatusCode() == 404) {
            printStatusMessage(result, "File not found in the server.");
            return;
        }

        String path;

        // Open a file explorer and set its default directory
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
        System.out.println("Type the new title for the file. Leave empty to not change it.");
        String title = br.readLine();
        System.out.println("Type a topic for the file. Leave empty to not change it.");
        DataFile.Topic topic = MediaClient.solveTopic(br.readLine());
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
        }
        else if(result.getStatusCode() >= 500)
        {
            printStatusMessage(result.getStatusCode(),
                    "Internal server error: " +(String)result.getContent());
        }
    }

    // endregion

    // region Media Handler subscription

    public static void subscribe(MediaHandler mediaHandler,
                              DatagramCertificate certificate)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


/*
        printStatusMessage(statusCode,
                "File [" + title + "] uploaded successfully.");*/
    }

    // endregion

    // region Utilities

    private static void printStatusMessage(int status, String message) {
        System.out.println("[" + status + "]: " + message);
    }

    private static void printStatusMessage(DatagramObject statusObject, String message) {
        System.out.println("[" + statusObject.getStatusCode() + "]: " + message);
    }
    // endregion
}
