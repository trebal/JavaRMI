package Logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MediaServerLauncher {

    private static int port = 7777;
    private static String address = "127.0.0.1";

    private static String configPath =
            "/home/rdc2/Escritorio/DC/A6/RMI_Client_Storage/config.cfg";

    public static void main(String args[]) throws MalformedURLException {
        // Load configuration file
        try {
            loadConfig(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            MediaHandlerImpl exportedObj = new MediaHandlerImpl();
            startRegistry(port);

            // Register the object under the name “some”
            String registryURL = "rmi://" + address + ":" + port + "/some";
            Naming.rebind(registryURL, exportedObj);
            System.out.println("Server ready.");
        }
        // The above call will throw an exception if the registry does not already exist
        catch (RemoteException e) {
            System.out.println("Exception catch." + e);
        }
    }

    /**
     * Start a RMI registry on the local host, if it does not already exists at the specified port number.
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
     * @param path The path where the file is located.
     * @throws IOException Throws this exception if the file cannot be readed.
     */
    private static void loadConfig(String path) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            address = br.readLine();
            port = Integer.valueOf(br.readLine());
            String userName = br.readLine();
            String userPass = br.readLine();
        } catch (FileNotFoundException e) {
            System.out.println("Error while trying to read config file:\n" + e);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
