package Server;

import Logic.DataFile;
import Logic.DatagramObject;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class MediaServerLauncher {

    private static String MEDIA_PATH;
    private static int port;
    private static String address;

    private static MediaHandlerServer exportedObj;
    private static NetworkNode debugNode;

    private static boolean running = true;

    public static void main(String args[])
            throws IOException, NotBoundException {

        // Load configuration file from parameter path
        loadConfig(args[0]);

        // Create and export the object
        try {
            exportedObj = new MediaHandlerServer(MEDIA_PATH);
            startRegistry(port);

            // Register the object
            String registryURL = getRegistryURL(address, port);
            Naming.rebind(registryURL, exportedObj);

            System.out.println("Server ready.");
        }
        // The above call will throw an exception if the registry does not already exist
        catch (RemoteException e) {
            System.out.println("Exception catch." + e);
        }



        // Try to join the web service
        if(WebServiceHandler.testWebService()) {
            WebServiceHandler.joinWebService();
            //WebServiceHandler.postContent();
        }
        else{
            System.out.println("Cannot connect to the web service.");
        }



        // TODO Move the command handling logic into a class
        // Create a buffered reader to read commands from the console
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));

        // Read and handle the commands
        while (running) {
            try {
                handleCommand(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Close buffer
        br.close();
        System.exit(0);
    }

    private static void handleCommand(String commandLine)
            throws IOException, NotBoundException {

        StringTokenizer tokenizer = new StringTokenizer(commandLine, " ");
        String command = tokenizer.nextToken();
        try {
            switch (command) {
                case "connect":
                    System.out.println("Connecting");
                    String nodeAddress = tokenizer.nextToken();
                    int nodePort = Integer.valueOf(tokenizer.nextToken());

                    String registryURL = getRegistryURL(nodeAddress, nodePort);
                    NetworkNode node = (NetworkNode) Naming.lookup(registryURL);
                    debugNode = (NetworkNode) node.join(exportedObj).getContent();
                    System.out.println("Connected");
                    break;

                case "exit":
                    // TODO Execute a DELETE server in the web service
                    running = false;
                    break;

                case "post":
                    DataFile PostDatafile = new DataFile(
                            "Edge of tomorrow",
                            DataFile.Topic.Action,
                            "The best movie ever dude.",
                            "GOD",
                            "");
                    WebServiceHandler.postContent(PostDatafile);
                    break;

                case "get":
                    WebServiceHandler.getServerList();
                    break;

                case "delete":
                    DataFile deleteDatafile = new DataFile(
                            "Edge of tomorrow",
                            DataFile.Topic.Action,
                            "The best movie ever dude.",
                            "GOD",
                            "");
                    WebServiceHandler.deleteContent(deleteDatafile);

                    break;

                case "put":
                    DataFile putDatafile = new DataFile(
                            "Edge of today",
                            DataFile.Topic.Action,
                            "The worst movie ever dude.",
                            "GOD",
                            "");
                    WebServiceHandler.putContent("Edge of tomorrow", "GOD", putDatafile);

                    break;

                default:
                    System.out.println("Unrecognized command: " + commandLine);
                    break;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    // region Server Utilities

    /**
     * Generates the URL corresponding to the RMI server entity with the
     * correct syntax.
     *
     * @param address The address of this server.
     * @param port    The port of this server.
     * @return The generated URL.
     */
    private static String getRegistryURL(String address, int port) {
        return "rmi://" + address + ":" + port + "/media";
    }

    /**
     * Start a RMI registry on the local host, if it does not already
     * exists at the specified port number.
     *
     * @param RMIPortNum The port of this RMI server.
     * @throws RemoteException throws this exception.
     */
    private static void startRegistry(int RMIPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();
        }
        // The above call will throw an exception if the registry does not already exist
        catch (RemoteException ex) {
            // No valid registry at that port.
            System.out.println("RMI registry cannot be located at port " + RMIPortNum);
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
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
            MEDIA_PATH = br.readLine();
            address = br.readLine();
            port = Integer.valueOf(br.readLine());
        } catch (FileNotFoundException e) {
            System.out.println("Error while trying to read config file:" +
                    "\n" + e);
            System.exit(1);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    // endregion

    public static String getAddress()
    {
        return address;
    }

    public static int getPort()
    {
        return port;
    }
}
