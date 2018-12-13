package Server;

import Client.MediaCallback;
import Logic.DataFile;
import Logic.Subscriber;
import Logic.User;
import Server.Database.UserHandler;

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
     * The list of subscribers. The list itself contains a sublist
     * for each topic.
     */
    private ArrayList<ArrayList<Subscriber>> subscribers;

    /**
     * The singleton constructor.
     */
    private SubscriptionHandler() {

        // Create a list for each topic
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
    public boolean addSubscriber(
            String username,
            DataFile.Topic topic,
            MediaCallback callback) {

        // Add only if the subscriber is not subscribed already
        if (getSubscriber(username, topic)!=null) {
            return false;
        } else {
            getSubscriptionListByTopic(topic).add(
                    new Subscriber(username,callback));
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
        List<Subscriber> subsList = getSubscriptionListByTopic(topic);

        Subscriber sub = getSubscriber(username, topic);
        if(subsList.contains(sub))
        {
            subsList.remove(getSubscriber(username, topic));
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Notifies all subscribers about a new file submission belonging to the topic .
     *
     * @param topic The Topic of the new uploaded file.
     * @param title The title of the new uploaded file.
     */
    public void notifySubscribers(DataFile.Topic topic, String title) {

        // Get the sublist of the topic subscribers
        ArrayList<Subscriber> subsList = SubscriptionHandler.handler.getSubscriptionListByTopic(topic);

        // For each user, call it back
        for (Subscriber sub : subsList) {

            MediaCallback callback = sub.getCallback();

            try {
                callback.notify(
                        "\t-New file of [" + topic + "] with title uploaded: " + title);
            } catch (RemoteException e) {
                System.out.println("Client could not be notified.");
            }
        }
    }

    /**
     * Returns the list of users subscribed to the passed topic.
     * The topic is converted from enum to an ordinal (integer),
     * which will act as an index to return the corresponding
     * sublist in the subscribers list.
     *
     * @param topic The topic subscription.
     * @return A list of users subscribed to this topic.
     */
    private ArrayList<Subscriber> getSubscriptionListByTopic(DataFile.Topic topic) {
        return subscribers.get(topic.ordinal());
    }

    /**
     * Returns a User, if exists, contained in the subscription topic
     * sublist.
     * @param username The username.
     * @param topic The Topic of the sublist.
     * @return A User.
     */
    private Subscriber getSubscriber(String username, DataFile.Topic topic)
    {
        List<Subscriber> subList = getSubscriptionListByTopic(topic);

        for(Subscriber subscriber : subList)
        {
            if(subscriber.getUsername().equals(username))
            {
                return subscriber;
            }
        }

        return null;
    }
}
