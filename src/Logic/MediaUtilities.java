package Logic;

public interface MediaUtilities {
    Boolean login(String username, String password);

    String sayHello() throws java.rmi.RemoteException;

    String ping() throws java.rmi.RemoteException;
}
