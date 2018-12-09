package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface to be used as callbacks from the server to the client.
 */
public interface MediaCallback extends Remote {

    void notify(String message) throws RemoteException;
}
