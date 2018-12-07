package Client;

import Utilities.DatagramObject;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MediaClientHandler {

    public void download(String title)
    {
        // Check if arguments are correct
        if (tokenizer.countTokens() != 1) {
            System.out.println("Invalid [download] use: " + commandLine);
        }

        String title = tokenizer.nextToken();
        DatagramObject result = mediaHandler.download(title, certificate);

        if (result.getStatusCode() == 404) {
            System.out.println("File not found in the server.");
            return;
        }

        String path;

        // Open a file explorer and set its default directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(
                new File(System.getProperty("user.home")));

        // Return the selected absolute path
        if (fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            System.out.println("Invalid/Null file selected.");
            break;
        }

        // Write the file
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(path));
            out.write((byte[]) result.getContent());
        } finally {
            if (out != null) out.close();
        }

        System.out.println("File [" + title + "] downloaded successfully.");
    }
}
