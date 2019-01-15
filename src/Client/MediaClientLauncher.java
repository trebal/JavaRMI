package Client;

import Logic.DatagramCertificate;
import Server.MediaHandler;
import Logic.DatagramObject;
import Logic.User;

import java.io.*;
import java.rmi.*;
import java.util.StringTokenizer;

public class MediaClientLauncher {

    private static String portNum;
    private static String address;
    private static DatagramCertificate certificate = null;

    private static boolean running = true;

    private static MediaCallbackClient mediaCallback;
    private static MediaHandler mediaHandler;

    public static void main(String args[]) throws IOException {

        // Load client configuration
        loadConfig(args[0]);

        // Get the remote handler and create the callback object
        try {
            String registryURL = "rmi://" + address + ":" + portNum + "/media";
            mediaHandler = (MediaHandler) Naming.lookup(registryURL);
            mediaCallback = new MediaCallbackClient();
        } catch (Exception e) {
            System.out.println("Exception in Client: " + e);
            throw new RemoteException();
        }

        // Create a buffered reader to read commands from the console
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

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
                    break;
                } else {
                    System.out.println("Wrong username or password. Try again.");
                }
            } catch (IOException e) {
                System.out.println("Could not log in the server.");
            }
        }

        // Get and handle the command
        int command;
        while(running)
        {
            System.out.println("Choose a command:");
            System.out.println("1. Search files");
            System.out.println("2. Upload a file");
            System.out.println("3. Download a file");
            System.out.println("4. Edit a file");
            System.out.println("5. Delete a file");
            System.out.println("6. Subscribe to a topic");
            System.out.println("7. Unsubscribe from a topic");
            System.out.println("8. Exit");

            // Try to parse the command to an integer
            try{
                command  = Integer.valueOf(br.readLine());

                if(command >= 1 && command <= 8)
                {
                    handleCommand(command);
                }
                else{
                    throw new IllegalArgumentException();
                }
            }
            // Throw exception if the command is not a number or it is out of range
            catch (Exception e)
            {
                System.out.println("Not a valid command number. " +
                        "Choose again a value between 1 and 8 (both included):");
            }
        }

        // Close buffer and finish client
        br.close();
        System.out.println("Closing client.");
        System.exit(0);
    }

    /**
     * Handles the command called by the user.
     *
     * @param command The integer value corresponding to the command.
     */
    private static void handleCommand(int command) throws IOException {

        switch (command) {
            case 1:
                MediaHandlerClient.get(
                        mediaHandler,
                        certificate);
                break;
            case 2:
                MediaHandlerClient.upload(
                        mediaHandler,
                        certificate);
                break;

            case 3:
                MediaHandlerClient.download(
                        mediaHandler,
                        certificate);
                break;

            case 4:
                MediaHandlerClient.edit(
                        mediaHandler,
                        certificate);
                break;

            case 5:
                MediaHandlerClient.delete(
                        mediaHandler,
                        certificate);
                break;

            case 6:
                MediaHandlerClient.subscribe(
                        mediaHandler,
                        mediaCallback,
                        certificate);
                break;

            case 7:
                MediaHandlerClient.unsubscribe(
                        mediaHandler,
                        certificate);
                break;

            case 8:
                running = false;
                break;

            default:
                throw new IllegalArgumentException("Non allowed command.");
        }
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
