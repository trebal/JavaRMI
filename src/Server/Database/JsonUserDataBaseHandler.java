package Server.Database;

import Logic.User;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class JsonUserDataBaseHandler extends UserDataBaseHandler {

    private static final String usersPath =
            "/home/rdc2/Escritorio/DC/A6/DB/Users.json";

    //private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void saveUser(User user) {
        throw new NotImplementedException();
    }

    @Override
    public List<User> getUsers() {
        throw new NotImplementedException();
    }
}
