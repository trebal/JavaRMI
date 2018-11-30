package Logic;

import Utilities.DataFile;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle the subscription system.
 */
public class SubscriptionHandler {

    /**
     * An eager initialization singleton to handle the subscription itself.
     */
    public static final SubscriptionHandler handler =
            new SubscriptionHandler();

    /**
     * The list of subscribers. The list itself contains a sublist for each topic.
     */
    private ArrayList<ArrayList<String>> subscribers;

    private SubscriptionHandler() {
        subscribers = new ArrayList<ArrayList<String>>();

        for (DataFile.Topic topic : DataFile.Topic.values()) {
            subscribers.add(new ArrayList<String>());
        }
    }

    /**
     * Adds a new subscriber to the specified Topic.
     *
     * @param username The username of the subscriber.
     * @param topic    The Topic where the user subscribes.
     * @return Returns true if the subscriber could be added because was not
     * int the list, false otherwise.
     */
    public boolean addSubscriber(String username, DataFile.Topic topic) {

        List<String> subsList = getSubscriptionList(topic);

        if (subsList.contains(username)) {
            return false;
        } else {
            subsList.add(username);
            return true;
        }
    }

    /**
     * Removes a subscriber from the specified Topic.
     *
     * @param username The username of the subscriber.
     * @param topic    The Topic where the user subscribes.
     * @return Returns true if the subscriber could be removed because it was
     * int the list, false otherwise.
     */
    public boolean removeSubscriber(String username, DataFile.Topic topic) {
        List<String> subsList = getSubscriptionList(topic);

        if (subsList.contains(username)) {
            subsList.remove(username);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the list of users subscribed to this topic.
     *
     * @param topic The topic subscription.
     * @return A list of users subscribed to this topic.
     */
    public ArrayList<String> getSubscriptionList(DataFile.Topic topic) {
        System.out.println(topic.ordinal());
        return subscribers.get(topic.ordinal());
    }
}
