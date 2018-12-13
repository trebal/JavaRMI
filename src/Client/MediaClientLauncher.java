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

        // Read and handle the commands
        System.out.println("Client ready.");
        while (running) {
            try {
                handleCommand(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        // Close buffer
        br.close();

        System.out.println("Closing client.");
        System.exit(0);
    }

    /**
     * Handles the command called by the user.
     *
     * @param commandLine The text line containing the command.
     */
    private static void handleCommand(String commandLine) throws IOException {

        StringTokenizer tokenizer = new StringTokenizer(commandLine, " ");
        String command = tokenizer.nextToken();

        switch (command) {
            case "upload":
                MediaHandlerClient.upload(
                        mediaHandler,
                        certificate);
                break;

            case "download":
                MediaHandlerClient.download(
                        mediaHandler,
                        certificate);
                break;

            case "edit":
                MediaHandlerClient.edit(
                        mediaHandler,
                        certificate);
                break;

            case "delete":
                MediaHandlerClient.delete(
                        mediaHandler,
                        certificate);
                break;

            case "subscribe":
                MediaHandlerClient.subscribe(
                        mediaHandler,
                        mediaCallback,
                        certificate);
                break;

            case "unsubscribe":
                MediaHandlerClient.unsubscribe(
                        mediaHandler,
                        certificate);
                break;

            case "get":
                MediaHandlerClient.get(
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
