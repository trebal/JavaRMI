package Logic;

import java.io.Serializable;

public class DatagramCertificate implements Serializable{

    /**
     * The user name.
     */
    private final String username;
    /**
     * The certificate used to validate any transaction.
     */
    private final int certificate;

    /**
     * The constructor of the class.
     * @param username The user name.
     * @param certificate The certificate, generated in the log in process.
     */
    public DatagramCertificate(String username, int certificate)
    {
        this.username = username;
        this.certificate = certificate;
    }

    // region Getters

    public String getUsername() {
        return username;
    }

    public int getCertificate() {
        return certificate;
    }

    // endregion
}
