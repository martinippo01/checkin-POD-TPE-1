package ar.edu.itba.pod.tpe1.data;

import ar.edu.itba.pod.tpe1.data.utils.Airline;
import ar.edu.itba.pod.tpe1.data.utils.Notification;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


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



}
