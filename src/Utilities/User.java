package Utilities;

import java.io.Serializable;

public class User implements Serializable{
    private final String username;
    private final String password;
    private int digitalCertificate;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

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

    public void setDigitalCertificate(int digitalCertificate)
    {
        this.digitalCertificate = digitalCertificate;
    }

    /**
     * Checks if the password passed by parameter corresponds with the password of this Utilities.User.
     *
     * @param password The password that the user typed from the client in order to authenticate.
     * @return True if the password equals to the password of this Utilities.User, false otherwise.
     */
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}
