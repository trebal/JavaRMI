package Logic;

import Utilities.DataFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MediaServerLauncher {

    private static int portNum = 7777;

    public static void main(String args[]) throws MalformedURLException {
        try {
            MediaHandlerImpl exportedObj = new MediaHandlerImpl();
            startRegistry(portNum);

            // Register the object under the name “some”
            String registryURL = "rmi://localhost:" + portNum + "/some";
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
            Registry registry= LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    }

    public void listRegistry()
    {
        throw new NotImplementedException();
    }

    private static int getRMIPortNum()
    {
        throw new NotImplementedException();
    }
}
