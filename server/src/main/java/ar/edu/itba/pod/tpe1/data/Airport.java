package ar.edu.itba.pod.tpe1.data;

import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.exceptions.CounterReleaseException;
import ar.edu.itba.pod.tpe1.data.utils.*;
import counter.CounterReservationServiceOuterClass;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


import java.util.stream.Collectors;

public class Airport {

    // Key: Booking - Value: a boolean
    private final ConcurrentHashMap<Booking, Boolean> bookingCodes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Flight, Airline> flights = new ConcurrentHashMap<>();

    // Key: Sector - Value: A list of range of sectors
    private final ConcurrentHashMap<Sector, List<RangeCounter>> sectors = new ConcurrentHashMap<>();

//    private final List<CheckIn> checkIns = new ArrayList<>();
    private final List<CounterServiceOuterClass.CheckInRecord> checkIns = Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger counterId = new AtomicInteger(1);

    private static Airport instance = null;


    private final Map<Sector, Queue<RequestedRangeCounter>> pendingRequestedCounters = new ConcurrentHashMap<>();

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
    // TODO: sync!!!
    public RangeCounter addCounters(String sectorName, int count) {
        Sector sector = Sector.fromName(sectorName);
        if (count <= 0 || !sectors.containsKey(sector)) {
            return null; // Failure: sector does not exist or invalid counter count
        }
        int firstId = counterId.getAndAdd(count);

        // Se which is the last counter number of the sector, and in that case expand the RangeCounter
        int lastCounterOfSector = -1;
        RangeCounter removeRangeCounter = null;
        for(RangeCounter rangeCounter : sectors.get(sector)){
            if(rangeCounter.getCounterTo() >= lastCounterOfSector) {
                lastCounterOfSector = rangeCounter.getCounterTo();
                removeRangeCounter = rangeCounter;
            }
            lastCounterOfSector = Math.max(rangeCounter.getCounterTo(), lastCounterOfSector);
        }
        if(lastCounterOfSector == firstId - 1){
            sectors.get(sector).remove(removeRangeCounter);
            RangeCounter newRangeCounter = new RangeCounter(removeRangeCounter.getCounterFrom(), firstId + count - 1);
            sectors.get(sector).add(newRangeCounter);
            // Make sure the elements are in the correct order
            Collections.sort(sectors.get(sector));
            return newRangeCounter;
        }else{
            RangeCounter newRangeCounter = new RangeCounter(firstId, firstId + count - 1);
                    // And the range of counters to the sector, from the last
            sectors.get(sector).add(newRangeCounter);
            // Make sure the elements are in the correct order
            Collections.sort(sectors.get(sector));
            return newRangeCounter; // Success, returns the first ID of the new counters
        }
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

    }

    public List<CounterServiceOuterClass.CounterInfo> queryCounters(String sector) {

        return null;
    }


    public List<CounterServiceOuterClass.CheckInRecord> queryCheckIns(String sector, String airline) {
        return new ArrayList<>();
    }

    public Map<Sector, List<RangeCounter>> getSectors() {
        //return Collections.unmodifiableMap(sectors);
        Map<Sector, List<RangeCounter>> toReturn;
        synchronized (sectors) {
            toReturn = new ConcurrentHashMap<>(sectors);
        }
        return toReturn;
    }

    // TODO: Needs synchronization
    public List<RequestedRangeCounter> getAssignedRangeCounters(String sectorName, int from, int to) {
//        Sector sector = new Sector(sectorName);
//        List<AssignedRangeCounter> toReturn = new ArrayList<>();
//
//        // In case the range does not exist or the range is invalid, it fails
//        if(!sectors.containsKey(sector) || from < to){
//            return null;
//        }
//
//        sectors.get(sector).forEach((rangeCounter) -> {
//            if(ra)
//        });
        return null;
    }


    public List<RequestedRangeCounter> listCounters(String sectorName, int from, int to) {
        Sector sector = new Sector(sectorName);
        List<RangeCounter> sectorCounters = sectors.get(sector);
        List<RequestedRangeCounter> out = new ArrayList<>();
        for(RangeCounter rangeCounter : sectorCounters) {
            for (RequestedRangeCounter counter : rangeCounter.getAssignedRangeCounters()) {
                if (counter.getCounterFrom() <= from && counter.getCounterTo() >= to) {
                    out.add(new RequestedRangeCounter(counter));
                }
            }
        }
        return out;
    }

    public FreeCounterResult freeCounters(String sectorName, int fromVal, String airlineName) throws CounterReleaseException {

        Sector sector = new Sector(sectorName);
        Airline airline = new Airline(airlineName);
        List<RangeCounter> sectorCounters = sectors.get(sector);

        if (sectorCounters == null) {
            throw new CounterReleaseException("Sector '" + sector + "' does not exist.");
        }

        RequestedRangeCounter rangeCounterFound = null;
        for (RangeCounter rangeCounter : sectorCounters) {
            if(rangeCounter.getCounterFrom() <= fromVal && rangeCounter.getCounterTo() >= fromVal){
                RequestedRangeCounter temp = rangeCounter.freeRange(fromVal, airline);
                if(temp != null) {
                    rangeCounterFound = temp;
                    break;
                }

            }
        }

        if(rangeCounterFound == null){
            throw new CounterReleaseException("No range starting at counter " + fromVal + " exists in sector '" + sectorName + "'.");

        }

        //TODO: VERIFICAR PERSONAS EN ESPERA

        return new FreeCounterResult(String.valueOf(rangeCounterFound.getCounterFrom()), rangeCounterFound.getCounterFrom(), rangeCounterFound.getCounterTo(), rangeCounterFound.getAirline().getName(), rangeCounterFound.getFlights().stream().map(Flight::getFlightCode).collect(Collectors.toList()));
    }

    public RequestedRangeCounter assignCounters(String sectorName, int count, String airlineName, List<String> flightsToReserve) {
        // Get the sector
        Sector sector = Sector.fromName(sectorName);
        if (!sectors.containsKey(sector)) {
            // Sector does not exist
            return null;
        }

        // Check if all flights are valid and linked to the specified airline
        Airline airline = new Airline(airlineName);
        List<Flight> validFlights = new ArrayList<>();
        for (String flightCode : flightsToReserve) {
            Flight flight = new Flight(flightCode);
            Airline registeredAirline = flights.getOrDefault(flight, null);
            if (registeredAirline == null || !registeredAirline.equals(airline)) {
                // If any flight does not exist or is registered to a different airline
                return null;
            }
            validFlights.add(flight);
        }



        // Attempt to find a contiguous block of counters
        List<RangeCounter> ranges = sectors.get(sector);
        RequestedRangeCounter assignedRangeCounter = null;
        for(RangeCounter rangeCounter : ranges){
            assignedRangeCounter = rangeCounter.assignRange(count, validFlights, airline);
            return assignedRangeCounter;
        }

        pendingRequestedCounters.putIfAbsent(sector, new ConcurrentLinkedQueue<>());
        pendingRequestedCounters.get(sector).add(new RequestedRangeCounter(validFlights, airline, true));
        return null;
    }

}

