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
        notifications.remove(airline);
        return true;
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

    public Notification getNotification(Airline airline){
        if(!notifications.containsKey(airline))
            // This can happen if the airline got unregistered, or it never was registered
            return null;
        return notifications.get(airline).poll();
    }


}
