package Client;

import Logic.DatagramCertificate;
import Logic.MediaCallbackClient;
import Logic.MediaHandler;
import Logic.MediaPackage;
import Utilities.DataFile;
import Utilities.DatagramObject;
import Utilities.User;

import java.io.*;
import java.rmi.*;
import java.util.List;
import java.util.StringTokenizer;

public class MediaClient {

    private static String portNum;// = "7777";
    private static String address;// = "127.0.0.1";
    private static String userName;// = "DefaultUser";
    private static String userPass;// = "1234";
    private static DatagramCertificate certificate = null;

    private static boolean running = true;
    private static final String DEFAULT_DIRECTORY =
            "user.home";
    private static final String configPath =
            "/home/rdc2/Escritorio/DC/A6/RMI_Client_Storage/config.cfg";

    private static MediaCallbackClient mediaCallback;
    private static MediaHandler mediaHandler;

    public static void main(String args[]) throws IOException {

        // Load client configuration
        loadConfig(configPath);

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
                userName = br.readLine();
                System.out.println("Type your password:");
                userPass = br.readLine();
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
                e.printStackTrace();
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
                System.out.println("command upload");
                MediaClientHandler.upload(
                        mediaHandler,
                        certificate);
                break;

            case "download":
                MediaClientHandler.download(
                        mediaHandler,
                        certificate);
                break;

            case "edit":
                MediaClientHandler.edit(
                        mediaHandler,
                        certificate);
                break;

            case "delete":
                MediaClientHandler.delete(
                        mediaHandler,
                        certificate);
                break;

            case "get": {
                if (tokenizer.countTokens() == 2) {
                    String mode = tokenizer.nextToken();
                    // By topic
                    if (mode.equals("topic")) {
                        DataFile.Topic topic = solveTopic(tokenizer.nextToken());
                        DatagramObject queryResult = mediaHandler.getContents(topic, certificate);

                        System.out.println("Search by topic result: ");
                        List<String> resultList = (List<String>) queryResult.getContent();
                        for (String title : resultList) {
                            System.out.println("\t-" + title);
                        }

                        return;
                    }
                    // By description
                    else if (mode.equals("description")) {
                        String text = tokenizer.nextToken();
                        DatagramObject queryResult = mediaHandler.getContents(text, certificate);

                        System.out.println("Search by description result: ");
                        List<String> resultList = (List<String>) queryResult.getContent();
                        for (String title : resultList) {
                            System.out.println("\t-" + title);
                        }

                        return;
                    }
                }
                System.out.println("Invalid [get] use: " + commandLine);
                break;
            }

            case "subscribe": {
                // Check if arguments are correct
                if (tokenizer.countTokens() != 1) {
                    System.out.println("Invalid [subscribe] use: " + commandLine +
                            ".\nUse the following syntax: subscribe <topic>");
                    return;
                }

                DataFile.Topic topic = solveTopic(tokenizer.nextToken());

                DatagramObject statusCode = mediaHandler.subscribe(
                        topic,
                        mediaCallback,
                        certificate);
                System.out.println(statusCodeToString(statusCode.getStatusCode()));
                break;
            }

            case "unsubscribe":
                // Check if arguments are correct
                if (tokenizer.countTokens() != 1) {
                    System.out.println("Invalid [unsubscribe] use: " + commandLine +
                            ".\nUse the following syntax: unsubscribe <topic>");
                    return;
                }

                DataFile.Topic topic = solveTopic(tokenizer.nextToken());

                DatagramObject statusCode = mediaHandler.unsubscribe(topic, certificate);
                System.out.println(statusCodeToString(statusCode.getStatusCode()));
                break;

            case "exit":
                running = false;
                break;

            case "debug":
                System.out.println("Nothing loaded in debug mode.");
                break;

            default:
                System.out.println("Unrecognized command: " + commandLine);
                break;
        }
    }

    /**
     * Converts an HTTP status code into a String.
     *
     * @param statusCode The numeric status code.
     * @return The String corresponding to the status code.
     */
    private static String statusCodeToString(int statusCode) {
        switch (statusCode) {
            case 201:
                return "(201) Operation accepted.";
            default:
                return "Unknown status code.";
        }
    }

    /**
     * Converts an String to a Topic. If the String matches the Topic, it will return the
     * corresponding topic, otherwise it will return the default value: Undefined.
     *
     * @param topic The String corresponding to the Topic name.
     * @return The Topic enum type corresponding to the String.
     */
    public static DataFile.Topic solveTopic(String topic) {
        // Normalize the topic to match the enum syntax.
        topic = topic.toLowerCase();
        topic = topic.substring(0, 1).toUpperCase() + topic.substring(1, topic.length());

        DataFile.Topic topicValue;

        // Try to convert it to the corresponding enum topic, and if it is not possible,
        // use the default value Undefined.
        try {
            topicValue = DataFile.Topic.valueOf(topic);
        } catch (IllegalArgumentException e) {
            System.out.println("Non recognized topic [" + topic + "]: using default [Undefined]");
            topicValue = DataFile.Topic.Undefined;
        }

        return topicValue;
    }

    /**
     * Loads the configuration contained in the configuration file.
     *
     * @param path The path where the file is located.
     * @throws IOException Throws this exception if the file cannot be readed.
     */
    private static void loadConfig(String path) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            address = br.readLine();
            portNum = br.readLine();
            userName = br.readLine();
            userPass = br.readLine();
        } catch (FileNotFoundException e) {
            System.out.println("Error while trying to read config file:\n" + e);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}

/*
JFrame frame = new JFrame("RMI Client");
frame.setContentPane(new ClientUI().panelMain);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.pack();
frame.setVisible(true);
*/
