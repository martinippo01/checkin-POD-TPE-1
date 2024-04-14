package ar.edu.itba.pod.tpe1.data;

import ar.edu.itba.pod.tpe1.data.utils.Airline;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class Notifications {

    private final Set<Airline> airlinesSubscribed = Collections.synchronizedSet(new HashSet<>());

    private static Notifications instance = null;

    public Notifications() {

    }

    public static synchronized Notifications getInstance(){
        if(instance == null){
            instance = new Notifications();
        }
        return instance;
    }

    public boolean notifyAirline(Airline airline){
        if(!airlinesSubscribed.contains(airline)){
            return false;
        }
        
    }

}
