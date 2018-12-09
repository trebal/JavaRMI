package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MediaCallback extends Remote {
    void notifySubscriber(String message) throws RemoteException;
}
