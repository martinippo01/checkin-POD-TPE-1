package ar.edu.itba.pod.tpe1.data;

import airport.AirportService;
import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.utils.*;
import ar.edu.itba.pod.tpe1.servant.CounterReservationService;
import counter.CounterReservationServiceOuterClass;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;


import java.util.stream.Collectors;

public class Airport {

    private final ConcurrentHashMap<String, String> flightToAirlineMap = new ConcurrentHashMap<>();
    //private final ConcurrentHashMap<String, Integer> bookingCodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Booking, Boolean> bookingCodes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Flight, Airline> flights = new ConcurrentHashMap<>();

    // Make the value of the map a sorted set of type <range>. Make the class range of counters
    private final ConcurrentHashMap<Sector, List<RangeCounter>> sectors = new ConcurrentHashMap<>();

    private final List<CounterServiceOuterClass.CheckInRecord> checkIns = Collections.synchronizedList(new ArrayList<>());
//    private final List<CheckIn> checkIns = new ArrayList<>();
    private final AtomicInteger counterId = new AtomicInteger(1);

    private static Airport instance = null;

    private Airport() {}

    public static synchronized Airport getInstance() {
        if (instance == null) {
            instance = new Airport();
        }
        return instance;
    }

    public List<CounterReservationServiceOuterClass.Sector> listSectors() {
        return new ArrayList<>();
    }

    // Adds a sector if it does not already exist
    public boolean addSector(String sectorName) {
        // Failure if sector already exists
        return sectors.putIfAbsent(new Sector(sectorName), new ArrayList<>()) == null;
    }

    // Adds a set of counters to a sector
    public Integer addCounters(String sectorName, int count) {
        Sector sector = Sector.fromName(sectorName);
        if (count <= 0 || !sectors.containsKey(sector)) {
            return null; // Failure: sector does not exist or invalid counter count
        }
        int firstId = counterId.getAndAdd(count);

        // And the range of counters to the sector
        sectors.get(sector).add(new RangeCounter(firstId, firstId + count - 1));



//        for (int i = firstId; i < firstId + count; i++) {
//            CounterServiceOuterClass.CounterInfo counter =  CounterServiceOuterClass.CounterInfo.newBuilder().setSector(sectorName).setRange(String.valueOf(i)).build();
//            counterDetails.get(sectorName).put(i, counter); // Initialize counters with no airline or flight
//        }
        return firstId; // Success, returns the first ID of the new counters
    }

    // Register a passenger, link booking and flight codes
    public boolean registerPassenger(String bookingCode, String flightCode, String airlineName) {

        Flight flight = new Flight(flightCode);
        Airline airline = new Airline(airlineName);
        Booking booking = new Booking(bookingCode, flight);

        if (bookingCodes.containsKey(booking)){ // In case the booking already exists, it fails
            return false;
        }

        // In case the flight exists
        if(flights.containsKey(flight) ){
            // Check if it belongs to other airline, in that case it fails
            if(!flights.get(flight).equals(airline))
                return false;
        }

        // If absent, put the flight
        flights.putIfAbsent(flight, airline);
        // Put the new booking code
        bookingCodes.put(booking, false);

        return true;
//        if(bookingCodes.putIfAbsent(booking, false) != null) {
//            return false;
//        }
//
//        if (bookingCodes.putIfAbsent(bookingCode, 1) != null) {
//            return false; // Failure, booking code already exists
//        }
//        if (flightToAirlineMap.putIfAbsent(flightCode, airlineName) != null &&
//                !flightToAirlineMap.get(flightCode).equals(airlineName)) {
//            bookingCodes.remove(bookingCode); // Roll back the booking code insertion
//            return false; // Failure, flight code mismatch
//        }
//        return true; // Success
    }

    public void logCheckIn(String sector, int counter, String airline, String flight, String booking) {
        synchronized (checkIns) {
            checkIns.add(CounterServiceOuterClass.CheckInRecord.newBuilder().setAirline(airline).setSector(sector).setCounter(counter).setFlight(flight).setBookingCode(booking).build());
        }
    }

    public List<CounterServiceOuterClass.CounterInfo> queryCounters(String sector) {
//
//        if (sector == null)
//            return new ArrayList<>();
//
//        //it should return all counters if sector is null
//        if(Objects.equals(sector, ""))
//            return counterDetails.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
//
//        return new ArrayList<>(counterDetails.get(sector).values());
        return null;
    }

    public List<CounterServiceOuterClass.CheckInRecord> querygit (String sector, String airline) {
        return checkIns.stream()
                .filter(c -> (sector == null || c.getSector().equals(sector)) && (airline == null || c.getAirline().equals(airline)))
                .collect(Collectors.toList());
    }

    public List<CounterServiceOuterClass.CheckInRecord> queryCheckIns(String sector, String airline) {
        return new ArrayList<>();
    }
}

