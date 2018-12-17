package Client;

import Server.MediaHandler;

import java.io.*;
import java.rmi.*;

public class MediaClientLauncher {

    private static String portNum;
    private static String address;

    public static void main(String[] args) throws IOException {

        // Load client configuration
        loadConfig(args[0]);

        // Get the remote handler and create the callback object
        MediaHandler mediaHandler;
        try {
            String registryURL = "rmi://" + address + ":" + portNum + "/media";
            mediaHandler = (MediaHandler) Naming.lookup(registryURL);
        } catch (Exception e) {
            System.out.println("Exception in Client: " + e);
            throw new RemoteException();
        }

        // Launch the Handler
        MediaHandlerClient handler = new MediaHandlerClient(mediaHandler);
        handler.run();

        System.out.println("Closing client.");
        System.exit(0);
    }

    /**
     * Loads the configuration contained in the configuration file.
     *
     * @param path The path where the file is located.
     * @throws IOException Throws this exception if the file cannot be read.
     */
    private static void loadConfig(String path) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            address = br.readLine();
            portNum = br.readLine();
        } catch (FileNotFoundException e) {
            System.out.println("Error while trying to read config file:\n" + e);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
