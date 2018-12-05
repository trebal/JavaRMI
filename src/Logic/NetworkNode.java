package Logic;

import Utilities.DatagramObject;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NetworkNode extends Remote {

    DatagramObject join(NetworkNode node) throws RemoteException;

    DatagramObject ping() throws RemoteException;
}
