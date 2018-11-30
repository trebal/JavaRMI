package Logic;

import Utilities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO Do the login here
public class ServerLoginHandler {

    private static int certificateLength = 8;
    private static Random random = new Random();

    private static List<User> activeUsers = new ArrayList<>();

    // TODO Get the user from the database
    public static User getUser(String userName) {
        return new User("DefaultUser", "1234");
    }

    public static int generateCertificate() {
        return (int) (random.nextInt() % Math.pow(10, certificateLength));
    }

    public static void addActiveUser(User user)
    {
        activeUsers.add(user);
    }
}
