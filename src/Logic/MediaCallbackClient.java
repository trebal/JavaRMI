package Logic;

import java.io.Serializable;
import java.rmi.RemoteException;

public class MediaCallbackClient implements MediaCallback, Serializable{

    public MediaCallbackClient()
    {

    }

    @Override
    public void notifySubscriber(String message) throws RemoteException {
        System.out.println("New notification from the server: " + message);
    }
}
