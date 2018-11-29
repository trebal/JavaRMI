package Logic;

import java.rmi.RemoteException;

public interface MediaCallback {
    void notifySubscriber(String message) throws RemoteException;
}
