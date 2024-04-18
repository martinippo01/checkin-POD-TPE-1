package ar.edu.itba.pod.tpe1.data;

import ar.edu.itba.pod.tpe1.data.utils.Airline;
import ar.edu.itba.pod.tpe1.data.utils.Notification;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class Notifications {

    private final ConcurrentHashMap<Airline, BlockingQueue<Notification>> notifications = new ConcurrentHashMap<>();

    private static Notifications instance = null;

    public Notifications() {}

    public static synchronized Notifications getInstance(){
        if(instance == null){
            instance = new Notifications();
        }
        return instance;
    }

    public boolean registerAirline(Airline airline){
        // TODO evaluate if airline exists!!!!
        if(notifications.containsKey(airline))
            return false;
        notifications.put(airline, new LinkedBlockingQueue<>());
        return true;
    }

    public boolean unregisterAirline(Airline airline){
        if(!notifications.containsKey(airline))
            return false;
        // TODO: Check if the addition should be .add or .put
        // Add to the queue a poisson pill, so when producer consumes it, will stop taking form the queue
        notifications.get(airline).add(new Notification.Builder().setPoisonPill().build());
        return true;
    }

    public void removeAirline(Airline airline){
        notifications.remove(airline);
    }

    public void notifyAirline(Airline airline, Notification notification){
        if(!notifications.containsKey(airline))
            throw new IllegalArgumentException();
        try {
            notifications.get(airline).put(notification);
        }catch (InterruptedException e){
            // TODO check this try catch and which is the best way to solve this
            e.printStackTrace();
            System.err.println("Could not queue notification");
        }
    }

    public Notification getNotification(Airline airline) throws InterruptedException {
        if(!notifications.containsKey(airline))
            // This can happen if the airline got unregistered, or it never was registered
            return null;
        // I'll block if there are no elementes in queue
        return notifications.get(airline).take();
    }


}
