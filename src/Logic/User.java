package Logic;

import Client.MediaCallback;

import java.io.Serializable;

// TODO Create a class named LoggedUser. It will contain an user and the certificate
public class User implements Serializable{

    /**
     * The user name.
     */
    private final String username;
    /**
     * The password of this user.
     */
    private final String password;
    /**
     * The digital certificate, generated after a login.
     */
    private int digitalCertificate;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        digitalCertificate = 0;
    }

    // region Getters

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getDigitalCertificate()
    {
        return digitalCertificate;
    }

    // endregion

    // region Setters

    public void setDigitalCertificate(int digitalCertificate)
    {
        this.digitalCertificate = digitalCertificate;
    }

    // endregion

    /**
     * Checks if the password passed by parameter corresponds with the password of this Logic.User.
     *
     * @param password The password that the user typed from the client in order to authenticate.
     * @return True if the password equals to the password of this Logic.User, false otherwise.
     */
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}
