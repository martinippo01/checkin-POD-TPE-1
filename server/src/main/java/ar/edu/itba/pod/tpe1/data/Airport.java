package ar.edu.itba.pod.tpe1.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class Airport {

    private final ConcurrentHashMap<String, String> flightToAirlineMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> bookingCodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> sectors = new ConcurrentHashMap<>();
    private final AtomicInteger counterId = new AtomicInteger(1);

    private static Airport instance = null;

    private Airport() {}

    public static synchronized Airport getInstance() {
        if (instance == null) {
            instance = new Airport();
        }
        return instance;
    }

    // Adds a sector if it does not already exist
    public boolean addSector(String sectorName) {
        if (sectors.putIfAbsent(sectorName, 0) == null) {
            return true; // Success
        }
        return false; // Failure, sector already exists
    }

    // Attempts to add a set of counters to a sector
    public Integer addCounters(String sectorName, int count) {
        if (!sectors.containsKey(sectorName) || count <= 0) {
            return null; // Failure: sector does not exist or invalid counter count
        }
        return counterId.getAndAdd(count); // Success, returns the first ID of the new counters
    }

    // Attempts to register a passenger with the given booking code and flight
    public boolean registerPassenger(String bookingCode, String flightCode, String airlineName) {
        if (!(bookingCodes.putIfAbsent(bookingCode, 1) == null)) {
            return false; // Failure, booking code already exists
        }
        if (!(flightToAirlineMap.putIfAbsent(flightCode, airlineName) == null) &&
                !flightToAirlineMap.get(flightCode).equals(airlineName)) {
            bookingCodes.remove(bookingCode); // Roll back the booking code insertion
            return false; // Failure, flight code mismatch
        }
        return true; // Success
    }
}
