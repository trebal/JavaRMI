package Logic;

import Utilities.DataFile;
import Utilities.User;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionHandler {
    private static List<String> subsAction = new ArrayList<>();
    private static List<String> subsUndefined = new ArrayList<>();

    public static boolean addSubscriptor(String username, DataFile.Topic topic) {

        List<String> subsList = getSubscriptionList(topic);

        if(subsList.contains(username))
        {
            return false;
        }
        else {
            subsList.add(username);
            return true;
        }
    }

    private static List<String> getSubscriptionList(DataFile.Topic topic) {
        switch (topic) {
            case Action:
                return subsAction;
            default:
                return subsUndefined;
        }
    }
}
