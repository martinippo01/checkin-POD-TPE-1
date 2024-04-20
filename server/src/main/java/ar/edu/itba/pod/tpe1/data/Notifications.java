package ar.edu.itba.pod.tpe1.data;

import airport.NotificationsServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.utils.Airline;
import ar.edu.itba.pod.tpe1.data.utils.Flight;
import ar.edu.itba.pod.tpe1.data.utils.Notification;
import ar.edu.itba.pod.tpe1.data.utils.Sector;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


public class Notifications {

    private final ConcurrentHashMap<Airline, BlockingQueue<Notification>> notifications = new ConcurrentHashMap<>();

    private static Notifications instance = null;
    private static Airport airport = Airport.getInstance();

    public Notifications() {}

    public static synchronized Notifications getInstance(){
        if(instance == null){
            instance = new Notifications();
        }
        return instance;
    }

    public boolean registerAirline(Airline airline){
        if(notifications.containsKey(airline) || airport.airlineExists(airline))
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
        notifications.get(airline).add(notification);
    }

    public Notification getNotification(Airline airline) throws InterruptedException {
        if(!notifications.containsKey(airline))
            // This can happen if the airline got unregistered, or it never was registered
            return null;
        // I'll block if there are no elementes in queue
        return notifications.get(airline).take();
    }

    public void notifyCountersAssigned(int from, int to, String sector, List<Flight> flights, Airline airline){
        notifyAirline(airline, new Notification.Builder()
                .setAirline(airline)
                .setSector(sector)
                .setFlights(flights.stream().map(Flight::getFlightCode).toList())
                .setCounterFrom(from)
                .setCounterTo(to)
                .setNotificationType(NotificationsServiceOuterClass.NotificationType.COUNTERS_ASSIGNED)
                .build()
        );
    }

    public void notifyPassengerEnteredQueue(Airline airline, String bookingCode, String flight, int counterFrom, int counterTo, String sector){
        notifyAirline(airline, new Notification.Builder()
                .setBooking(bookingCode)
                .setFlight(flight)
                .setAirline(airline)
                .setCounterFrom(counterFrom)
                .setCounterTo(counterTo)
                .setSector(sector)
                .setNotificationType(NotificationsServiceOuterClass.NotificationType.NEW_BOOKING_IN_QUEUE)
                .build()
        );
    }

    public void notifyCheckIn(Airline airline, String bookingCode, String flight, int counter){
        notifyAirline(airline, new Notification.Builder()
                .setAirline(airline)
                .setBooking(bookingCode)
                .setFlight(flight)
                .setCounter(counter)
                .setNotificationType(NotificationsServiceOuterClass.NotificationType.CHECK_IN_SUCCESSFUL)
                .build()
        );
    }

    public void notifyCountersRemoved(Airline airline, List<Flight> flights, int counterFrom, int counterTo, String sector){
        notifyAirline(airline, new Notification.Builder()
                .setAirline(airline)
                .setFlights(flights.stream().map(Flight::getFlightCode).toList())
                .setCounterFrom(counterFrom)
                .setCounterTo(counterTo)
                .setSector(sector)
                .setNotificationType(NotificationsServiceOuterClass.NotificationType.COUNTERS_REMOVED)
                .build()
        );
    }

    public void notifyCountersPending(Airline airline, int count, String sector, List<Flight> flights, int pendingAhead){
        notifyAirline(airline, new Notification.Builder()
                .setCounter(count)
                .setSector(sector)
                .setFlights(flights.stream().map(Flight::getFlightCode).toList())
                .setPendingAhead(pendingAhead)
                .setNotificationType(NotificationsServiceOuterClass.NotificationType.COUNTERS_PENDING)
                .build()
        );
    }

    public void notifyCountersPendingUpdate(Airline airline, int count, String sector, List<Flight> flights, int pendingAhead){
        notifyAirline(airline, new Notification.Builder()
                .setCounter(count)
                .setSector(sector)
                .setFlights(flights.stream().map(Flight::getFlightCode).toList())
                .setPendingAhead(pendingAhead)
                .setNotificationType(NotificationsServiceOuterClass.NotificationType.COUNTERS_UPDATE)
                .build()
        );
    }

}
