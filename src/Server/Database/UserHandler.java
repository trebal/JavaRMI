package Server.Database;

import Logic.User;

/**
 * An "static" class that manages the login from the users.
 */
public class UserHandler {

    /**
     * Tries to authenticate a user by first getting the Logic.User from the database (if exists), and then, checking if
     * the password is correct.
     *
     * @param username The user name of this Logic.User.
     * @param password The password corresponding to this user.
     * @return True if the user exists and the password is correct, false otherwise.
     */
    public static boolean login(String username, String password) {
        User user = getUser(username);

        return user != null && user.authenticate(password);
    }

    // TODO Do a database search to get this user, if exists

    /**
     * Extracts and returns a User from the user database, if exists. The search is done by the user name.
     *
     * @param username The user name of the Logic.User.
     * @return Returns either a Logic.User corresponding to the name, or a null object.
     */
    public static User getUser(String username) {
        // TODO Remove the hardcoded users
        switch (username) {
            case "1":
                return new User("1", "1");
            case "admin":
                return new User("admin", "1234");
            default:
                return null;
        }
    }
}
