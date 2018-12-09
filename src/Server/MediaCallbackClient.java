package Server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MediaCallbackClient extends UnicastRemoteObject implements MediaCallback, Serializable{

    public MediaCallbackClient() throws RemoteException {}

    @Override
    public void notifySubscriber(String message) throws RemoteException {
        System.out.println("New notification from the server:\n" + message);
    }
}
