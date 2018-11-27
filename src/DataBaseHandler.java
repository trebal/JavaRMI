import Utilities.User;

import java.util.List;

public interface DataBaseHandler {
    void saveUser(User user);
    List<User> getUsers();
}
