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

    public boolean containsSector(String sectorName) {
        return sectors.containsKey(sectorName);
    }

    public void putSector(String sectorName, int value) {
        sectors.put(sectorName, value);
    }

    public boolean addBookingCode(String bookingCode) {
        return bookingCodes.putIfAbsent(bookingCode, 1) == null;
    }

    public void removeBookingCode(String bookingCode) {
        bookingCodes.remove(bookingCode);
    }

    public boolean setFlightToAirline(String flightCode, String airlineName) {
        String existing = flightToAirlineMap.putIfAbsent(flightCode, airlineName);
        return existing == null || existing.equals(airlineName);
    }

    public int addCounters(int count) {
        int firstId = counterId.getAndAdd(count);
        return firstId;
    }
}
