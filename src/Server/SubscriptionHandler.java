package Server;

import Client.MediaCallback;
import Logic.DataFile;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle the subscription system. This class acts as a singleton.
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

    /**
     * The singleton constructor.
     */
    private SubscriptionHandler() {
        subscribers = new ArrayList<>();

        for (DataFile.Topic topic : DataFile.Topic.values()) {
            subscribers.add(new ArrayList<>());
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

        // Create a sublist containing the subscribers of the topic
        List<String> subsList = getSubscriptionListByTopic(topic);

        // Add only if the subscriber is not subscribed already
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

        // Create a sublist containing the subscribers of the topic
        List<String> subsList = getSubscriptionListByTopic(topic);

        // User found, return and validate
        if (subsList.contains(username)) {
            subsList.remove(username);
            return true;
        }
        // User not found, return false
        else {
            return false;
        }
    }
    /*
    public void notifySubscribers(DataFile.Topic topic, String title) {
        ArrayList<String> subsList = SubscriptionHandler.handler.getSubscriptionListByTopic(topic);

        for (MediaCallback callback : clientCallback) {
            try {
                callback.notify(
                        "\t-New file of [" + topic + "] with title uploaded: " + title);
            } catch (RemoteException e) {
                System.out.println("Client could not be notified.");
            }
        }
    }*/

    /**
     * Returns the list of users subscribed to the passed topic.
     * The topic is converted from enum to an ordinal (integer),
     * which will act as an index to return the corresponding
     * sublist in the subscribers list.
     *
     * @param topic The topic subscription.
     * @return A list of users subscribed to this topic.
     */
    public ArrayList<String> getSubscriptionListByTopic(DataFile.Topic topic) {
        return subscribers.get(topic.ordinal());
    }
}
