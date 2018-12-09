package Server.Database;

import Logic.User;

import java.util.ArrayList;
import java.util.List;

public abstract class UserDataBaseHandler {

    /**
     * A list of all existing users.
     */
    private ArrayList<User> users;



    /**
     * Stores a new user in the database.
     * @param user The User object.
     */
    public abstract void saveUser(User user);

    /**
     * Retrieves all the users from the database.
     * @return The list of all the users.
     */
    public abstract List<User> getUsers();
}
