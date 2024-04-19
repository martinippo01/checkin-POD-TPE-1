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

    private final ConcurrentHashMap<String, String> flightToAirlineMap = new ConcurrentHashMap<>();
    // Key: Booking - Value: a boolean that
    private final ConcurrentHashMap<Booking, Boolean> bookingCodes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Flight, Airline> flights = new ConcurrentHashMap<>();

    // Key: Sector - Value: A list of range of sectors
    private final ConcurrentHashMap<Sector, List<RangeCounter>> sectors = new ConcurrentHashMap<>();

    private final List<CounterServiceOuterClass.CheckInRecord> checkIns = Collections.synchronizedList(new ArrayList<>());
//    private final List<CheckIn> checkIns = new ArrayList<>();
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
    public Integer addCounters(String sectorName, int count) {
        Sector sector = Sector.fromName(sectorName);
        if (count <= 0 || !sectors.containsKey(sector)) {
            return null; // Failure: sector does not exist or invalid counter count
        }
        int firstId = counterId.getAndAdd(count);
        // TODO: Implement condition where if sector has counters (2-4) and first Id is 5, should create a contiguos sector (2-7)
        // And the range of counters to the sector
        sectors.get(sector).add(new RangeCounter(firstId, firstId + count - 1));

        // Make sure the elements are in the correct order
        Collections.sort(sectors.get(sector));

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

    }

    public void logCheckIn(String sector, int counter, String airline, String flight, String booking) {
        synchronized (checkIns) {
            checkIns.add(CounterServiceOuterClass.CheckInRecord.newBuilder().setAirline(airline).setSector(sector).setCounter(counter).setFlight(flight).setBookingCode(booking).build());
        }
    }

    public List<CounterServiceOuterClass.CounterInfo> queryCounters(String sector) {



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

    FreeCounterResult freeCounters(String sector, int fromVal, String airlineName) throws CounterReleaseException {
        List<RangeCounter> sectorCounters = sectors.get(sector);

        if (sectorCounters == null) {
            throw new CounterReleaseException("Sector '" + sector + "' does not exist.");
        }

        Optional<RangeCounter> range = sectorCounters.stream()
                .filter(r -> r.getCounterFrom() <= fromVal && r.getCounterTo() >= fromVal)
                .findFirst();

        if (!range.isPresent()) {
            throw new CounterReleaseException("No range starting at counter " + fromVal + " exists in sector '" + sectorName + "'.");
        }

        RangeCounter foundRange = range.get();
        if (!foundRange.get().equals(airlineName)) {
            throw new CounterReleaseException("Range counters are not assigned to '" + airlineName + "'.");
        }

        Queue<RequestedRangeCounter> queue = pendingRequestedCounters.get(sector);
        if (queue != null && queue.stream().anyMatch(req -> req.getCounterFrom().overlaps(foundRange))) {
            throw new CounterReleaseException("There are passengers waiting to be attended at the counters.");
        }

        sectorCounters.remove(foundRange);  // Successfully freeing the range
        return new FreeCounterResult(sector, foundRange.getCounterFrom(), foundRange.getCounterTo(), airlineName, foundRange.getAssignedRangeCounters().stream().toList().stream().map(requestedRangeCounter -> requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).collect(Collectors.joining())).collect(Collectors.toList()));
    }

    public RequestedRangeCounter assignCounters(String sectorName, int count, String airlineName, List<String> flightsToReserve) throws CounterReleaseException {
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

