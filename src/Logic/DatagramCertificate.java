package Logic;

import java.io.Serializable;

public class DatagramCertificate implements Serializable{

    private final String userName;
    private final int certificate;

    public DatagramCertificate(String userName, int certificate)
    {
        this.userName = userName;
        this.certificate = certificate;
    }

    public String getUsername() {
        return userName;
    }

    public int getCertificate() {
        return certificate;
    }
}
