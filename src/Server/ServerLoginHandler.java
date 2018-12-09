package Server;

import Logic.DatagramCertificate;
import Logic.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO Do the login here
public class ServerLoginHandler {

    /**
     * The size of the digital certificate which will be provided to the users.
     */
    private static final int certificateLength = 8;
    /**
     * The random seed to generate certificates.
     */
    private static Random random = new Random();
    /**
     * A list of the active users.
     */
    private static List<User> activeUsers = new ArrayList<>();

    // TODO Get the user from the database

    /**
     * Extracts a user from the data base.
     *
     * @param userName The name of the User.
     * @return A User object.
     */
    public static User getUserFromDB(String userName) {
        if(userName.equals("1"))
        {
           return new User("1","1");
        }
        return new User("DefaultUser", "1234");
    }

    /**
     * Generates a digital certificate for a User.
     *
     * @return The generated certificate.
     */
    public static int generateCertificate() {
        return (int) (random.nextInt() % Math.pow(10, certificateLength));
    }

    /**
     * Validates the certificate of the corresponding user.
     *
     * @param certificate The digital certificate of the User.
     * @return Returns true if the passed certificated corresponds to the
     * current certificate assigned by the server, false otherwise.
     */
    public static boolean validateCertificate(DatagramCertificate certificate) {
        for (User user : activeUsers) {
            if (user.getUsername().equals(certificate.getUsername())
                    && user.getDigitalCertificate() == certificate.getCertificate()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a User to the active User list.
     *
     * @param user The User to add.
     */
    public static void addActiveUser(User user) {
        activeUsers.add(user);
    }
}
