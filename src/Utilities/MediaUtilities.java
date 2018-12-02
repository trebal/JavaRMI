package Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A class to handle utilities for both client and server.
 */
public class MediaUtilities {
    /**
     * Converts a file located in the path into an array of Bytes.
     *
     * @param path The path where the file is located.
     * @return An array of bytes that encodes the file.
     * @throws IOException Throws this exception.
     */
    public static byte[] convertToByes(String path) throws IOException {
        System.out.println(path);
        File file = new File(path);
        return Files.readAllBytes(file.toPath());
    }
}
