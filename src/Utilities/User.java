package Utilities;

public class User {
    private final String username;
    private final String password;

    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    /**
     * Checks if the password passed by parameter corresponds with the password of this Utilities.User.
     * @param password The password that the user typed from the client in order to authenticate.
     * @return True if the password equals to the password of this Utilities.User, false otherwise.
     */
    public boolean authenticate(String password)
    {
        return this.password.equals(password);
    }
}
