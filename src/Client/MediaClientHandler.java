package Client;

import Logic.DatagramCertificate;
import Logic.MediaHandler;
import Logic.MediaPackage;
import Utilities.DataFile;
import Utilities.DatagramObject;
import Utilities.MediaUtilities;

import javax.swing.*;
import java.io.*;

public class MediaClientHandler {

    private static final String DEFAULT_DIRECTORY =
            "user.home";

    // region Media Handler main commands

    /**
     * Handles the upload command, which uploads the selected file with the chosen
     * parameters.
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

        System.out.println("Status: " + (statusCode.getStatusCode()));
        br.close();
    }

    /**
     * Handles the download command, which downloads a file from the server by the title.
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

        printStatusMessage(result,"File [" + title + "] downloaded successfully.");
    }



    // endregion

    // region Utilities

    private static void printStatusMessage(int status, String message)
    {
        System.out.println("[" + status + "]: " + message);
    }

    private static void printStatusMessage(DatagramObject statusObject, String message)
    {
        System.out.println("[" + statusObject.getStatusCode() + "]: " + message);
    }
    // endregion
}
