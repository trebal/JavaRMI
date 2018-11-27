import Utilities.User;

/**
 * An "static" class that manages the login from the users.
 */
public class UserLogin {

    /**
     * Tries to authenticate a user by first getting the Utilities.User from the database (if exists), and then, checking if
     * the password is correct.
     * @param username The user name of this Utilities.User.
     * @param password The password corresponding to this user.
     * @return True if the user exists and the password is correct, false otherwise.
     */
    public static boolean login(String username, String password) {
        User user = getUser(username);

        if (user == null) {
            return false;
        } else {
            return user.authenticate(password);
        }
    }

    // TODO Do a database search to get this user, if exists

    /**
     * Extracts and returns a Utilities.User from the user database, if exists. The search is done by the user name.
     * @param username The user name of the Utilities.User.
     * @return Returns either a Utilities.User corresponding to the name, or a null object.
     */
    private static User getUser(String username)
    {
        return new User("Felatore", "123");
    }
}
