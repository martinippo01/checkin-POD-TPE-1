package ar.edu.itba.pod.tpe1.data;

import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.utils.*;
import counter.CounterReservationServiceOuterClass;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


import java.util.stream.Collectors;

public class Airport {

    private final Notifications notifications = Notifications.getInstance();

    // Key: Booking - Value: a boolean
    private final ConcurrentHashMap<Booking, Boolean> bookingCodes = new ConcurrentHashMap<>();

    // Key: Flight - Value: An airline that
    private final ConcurrentHashMap<Flight, Airline> flights = new ConcurrentHashMap<>();

    private final Set<Airline> airlines = Collections.synchronizedSet(new HashSet<>());

    // Key: Sector - Value: A list of range of sectors
    private final ConcurrentHashMap<Sector, List<RangeCounter>> sectors = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Flight, Boolean> flightWasAssigned = new ConcurrentHashMap<>();

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

    public void addSector(String sectorName) throws Exception {
        if(sectors.putIfAbsent(new Sector(sectorName), new ArrayList<>()) != null)
            return;
        pendingRequestedCounters.put(new Sector(sectorName), new ConcurrentLinkedQueue<>());
    }

    // Adds a set of counters to a sector
    // TODO: sync!!!
    public RangeCounter addCounters(String sectorName, int count) {
        Sector sector = Sector.fromName(sectorName);
        if (count <= 0 || !sectors.containsKey(sector)) {
            throw new IllegalStateException("Invalid number of counters (must be positive)/sector does not exist.");
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
            RangeCounter newRangeCounter = new RangeCounter(removeRangeCounter, firstId + count - 1);
            sectors.get(sector).add(newRangeCounter);
            // Make sure the elements are in the correct order
            Collections.sort(sectors.get(sector));
            tryToAssignPendings(sector); // If there were pending assignments, try to solve them
            return newRangeCounter;
        }else{
            RangeCounter newRangeCounter = new RangeCounter(firstId, firstId + count - 1);
                    // And the range of counters to the sector, from the last
            sectors.get(sector).add(newRangeCounter);
            // Make sure the elements are in the correct order
            Collections.sort(sectors.get(sector));
            tryToAssignPendings(sector); // If there were pending assignments, try to solve them
            return newRangeCounter; // Success, returns the first ID of the new counters
        }
    }

    // Register a passenger, link booking and flight codes
    public void registerPassenger(String bookingCode, String flightCode, String airlineName) throws Exception {

        Flight flight = new Flight(flightCode);
        Airline airline = new Airline(airlineName);
        Booking booking = new Booking(bookingCode, flight);

        if (bookingCodes.containsKey(booking)){ // In case the booking already exists, it fails
            throw new IllegalArgumentException("Booking code already exists.");
        }

        // In case the flight exists
        if(flights.containsKey(flight) ){
            // Check if it belongs to other airline, in that case it fails
            if(!flights.get(flight).equals(airline))
                throw new IllegalCallerException("The flight is already registered to another airline.");
        }

        // If absent, put the flight and mark as it is not assigned yet
        flights.putIfAbsent(flight, airline);
        flightWasAssigned.putIfAbsent(flight, false);
        airlines.add(airline);
        // Put the new booking code
        bookingCodes.put(booking, false);
    }

    public List<CounterServiceOuterClass.CounterInfo> queryCountersBySector(String sectorName) throws RuntimeException {

        //This
        if(sectors.isEmpty()) {
            throw new IllegalStateException("There are no sectors registered at the airport.");
        }

        if(!sectorName.equals("")) {
            return queryCounters(sectorName, false).stream().map(
                    requestedRangeCounter -> CounterServiceOuterClass.CounterInfo.newBuilder()
                            .setSector(requestedRangeCounter.getCounterFrom() + "-" + requestedRangeCounter.getCounterTo())
                            .setAirline(requestedRangeCounter.getAirline().getName())
                            .addAllFlights(requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).toList())
                            .setWaitingPeople(requestedRangeCounter.getRequestedRange())
                            .setSector(sectorName)
                            .build()
            ).toList();
        }

        List<CounterServiceOuterClass.CounterInfo> out = new ArrayList<>();

        for (Sector sector : sectors.keySet()) {
            String sectorName2 = sector.getName();
            List<RequestedRangeCounter> requestedRangeCounters = queryCounters(sectorName2, true);

            for(RequestedRangeCounter requestedRangeCounter : requestedRangeCounters){
                CounterServiceOuterClass.CounterInfo counterInfo = CounterServiceOuterClass.CounterInfo.newBuilder()
                            .setSector(requestedRangeCounter.getCounterFrom() + "-" + requestedRangeCounter.getCounterTo())
                            .setAirline(requestedRangeCounter.getAirline().getName())
                            .addAllFlights(requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).toList())
                            .setWaitingPeople(requestedRangeCounter.getRequestedRange())
                            .setSector(sector.getName())
                            .build();
                out.add(counterInfo);
            }
        }

        return out;
    }

    public List<RequestedRangeCounter> queryCounters(String sectorName, Boolean allCounters) throws RuntimeException {

        Sector sector = new Sector(sectorName);
        List<RangeCounter> sectorCounters = sectors.getOrDefault(sector, new ArrayList<>());

        //a small patch, really hard bug to catch
        if(sectorCounters.isEmpty() && !allCounters) {
            throw new IllegalArgumentException("No counters found for the specified sector.");
        }

        List<RequestedRangeCounter> out = new ArrayList<>();
        boolean containsAssignedRangeCounter = false;

        for(RangeCounter rangeCounter : sectorCounters) {
                int prevFrom = rangeCounter.getCounterFrom();
                for (RequestedRangeCounter counter : rangeCounter.getAssignedRangeCounters()) {
                    if (prevFrom < counter.getCounterFrom())
                        out.add(new RequestedRangeCounter(prevFrom, counter.getCounterFrom() - 1, new ArrayList<>(), new Airline(""), false));
                        containsAssignedRangeCounter = true;
                        out.add(new RequestedRangeCounter(counter));
                    prevFrom = counter.getCounterTo() + 1;
                }
                if (prevFrom <= rangeCounter.getCounterTo())
                    out.add(new RequestedRangeCounter(prevFrom, rangeCounter.getCounterTo(), new ArrayList<>(), new Airline(""), false));
        }

        return containsAssignedRangeCounter ? out : new ArrayList<>();
    }

    public List<CounterServiceOuterClass.CheckInRecord> queryCheckIns(String sector, String airline) {
        return new ArrayList<>();
    }

    public Map<Sector, List<RangeCounter>> getSectors() {

        if(sectors.isEmpty())
            throw new IllegalStateException("There are no sectors registered at the airport.");

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

        // If sector does not exist or range is not valid, fail
        if(!sectors.containsKey(sector))
            throw new IllegalStateException("Invalid sector");

        if(to < from)
            throw new IllegalArgumentException("Invalid range.");

        List<RangeCounter> sectorCounters = sectors.get(sector);
        List<RequestedRangeCounter> out = new ArrayList<>();
        boolean containsAssignedRangeCounter = false;

        for(RangeCounter rangeCounter : sectorCounters) {
            if(!(to <= rangeCounter.getCounterFrom() || from >= rangeCounter.getCounterTo())) { // In case the range is outside the from-to
                int prevFrom = rangeCounter.getCounterFrom();
                for (RequestedRangeCounter counter : rangeCounter.getAssignedRangeCounters()) {
                    if (prevFrom < counter.getCounterFrom())
                        out.add(new RequestedRangeCounter(prevFrom, counter.getCounterFrom() - 1, new ArrayList<>(), new Airline(""), false));
                    if (counter.getCounterFrom() >= from && counter.getCounterTo() <= to) {
                        containsAssignedRangeCounter = true;
                        out.add(new RequestedRangeCounter(counter));
                    }
                    prevFrom = counter.getCounterTo() + 1;
                }
                if (prevFrom <= rangeCounter.getCounterTo())
                    out.add(new RequestedRangeCounter(prevFrom, rangeCounter.getCounterTo(), new ArrayList<>(), new Airline(""), false));
            }
        }
        return containsAssignedRangeCounter ? out : new ArrayList<>();
    }

    public FreeCounterResult freeCounters(String sectorName, int fromVal, String airlineName) throws Exception {

        Sector sector = new Sector(sectorName);
        Airline airline = new Airline(airlineName);
        List<RangeCounter> sectorCounters = sectors.getOrDefault(sector, new ArrayList<>());

        if (sectorCounters.isEmpty()) {
            throw new ClassNotFoundException("Sector '" + sectorName + "' does not exist.");
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
            throw new IllegalArgumentException("No range starting at counter " + fromVal + " exists in sector '" + sectorName + "'.");
        }else{
            // Notify the airline, tha counters where removed
            notifications.notifyCountersRemoved(rangeCounterFound.getAirline(), rangeCounterFound.getFlights(), rangeCounterFound.getCounterFrom(), rangeCounterFound.getCounterTo(), sectorName);
        }

        //TODO: VERIFICAR PERSONAS EN ESPERA
        boolean waiting = false;
        if(waiting)
            throw new IllegalStateException("Cannot free counters as there are passengers waiting to be attended.");

        // Attempt to assign from the queue when a sector was freed
        tryToAssignPendings(sector);

        return new FreeCounterResult(String.valueOf(rangeCounterFound.getCounterFrom()), rangeCounterFound.getCounterFrom(), rangeCounterFound.getCounterTo(), rangeCounterFound.getAirline().getName(), rangeCounterFound.getFlights().stream().map(Flight::getFlightCode).collect(Collectors.toList()));
    }

    public RequestedRangeCounter assignCounters(String sectorName, int count, String airlineName, List<String> flightsToReserve) {

        // Get the sector
        Sector sector = Sector.fromName(sectorName);
        if (!sectors.containsKey(sector)) {
            // Sector does not exist
            throw new IllegalArgumentException();
        }
        Airline airline = new Airline(airlineName);

        // Validate that the flights are correct
        List<Flight> validFlights = checkFlights(sector, airline, flightsToReserve);
        // In case at least one of the flights is not valid for any reason, fail
        if(validFlights == null)
            throw new IllegalArgumentException();

        // Attempt to assign a range at the requested sector
        RequestedRangeCounter assigned = findSpaceForRange(sector, validFlights, airline, count);

        // In case there was no space for the range, append it to the queue
        if(assigned == null) {
            pendingRequestedCounters.putIfAbsent(sector, new ConcurrentLinkedQueue<>());
            pendingRequestedCounters.get(sector).add(new RequestedRangeCounter(validFlights, airline, true, count));
            notifications.notifyCountersPending(airline, count, sectorName, validFlights, 0); // TODO: send proper pending ahead
            return null;
        }else{
            notifications.notifyCountersAssigned(assigned.getCounterFrom(), assigned.getCounterTo(), sectorName, assigned.getFlights(), airline);
        }
        return assigned;

    }

    private List<Flight> checkFlights(Sector sector, Airline airline, List<String> flightsToReserve){
        /* Check conditions for flights:
            + Check if there are passengers expected for each flight
            + Check if the flight code does not belong to another airline
            + There are no counters for the flight code somewhere else
            + There are no pending assignations that have the flight code in it
            + There were no assignations in the past for that airline
         */

        List<Flight> validFlights = new ArrayList<>();
        for (String flightCode : flightsToReserve) {

            Flight flight = new Flight(flightCode);
            Airline registeredAirline = flights.getOrDefault(flight, null);

            // Check Flight exists and is registered to the same airline and was not assigned before
            if (registeredAirline == null || !registeredAirline.equals(airline) || flightWasAssigned.get(flight)) { // Flight does not exist or Registered to a different airline
                return null;
            }

            // For each sector of the airport
            for(Sector otherSector: sectors.keySet()){

                // Check that the flight is not assigned in another counter
                for(RangeCounter rangeCounter: sectors.getOrDefault(otherSector, new ArrayList<>())){
                    for(RequestedRangeCounter counter: rangeCounter.getAssignedRangeCounters()){
                        if(counter.getFlights().contains(flight))
                            return null;
                    }
                }

                // Check that there is no pending assignation with the flight
                for(RequestedRangeCounter pendingAssignation : pendingRequestedCounters.get(sector)){
                    if(pendingAssignation.getFlights().contains(flight))
                        return null;
                }
            }

            validFlights.add(flight);
        }

        for (Flight flight : validFlights) {
            // Mark the flight as assigned
            flightWasAssigned.put(flight, Boolean.TRUE);
        }

        return validFlights;
    }

    private RequestedRangeCounter findSpaceForRange(Sector sector, List<Flight> flights, Airline airline, int count){
        List<RangeCounter> ranges = sectors.get(sector);
        RequestedRangeCounter assignedRangeCounter = null;
        for(RangeCounter rangeCounter : ranges){
            assignedRangeCounter = rangeCounter.assignRange(count, flights, airline);
            if(assignedRangeCounter != null)
                return assignedRangeCounter;
        }
        return null;
    }

    public List<RequestedRangeCounter> listPendingRequestedCounters(String sectorName) {
        Sector sector = new Sector(sectorName);
        Queue<RequestedRangeCounter> requestedRangeCounters = pendingRequestedCounters.getOrDefault(sector, new ArrayDeque<>());
        if(requestedRangeCounters.isEmpty())
            throw new IllegalArgumentException("Invalid sector.");
        return new ArrayList<>(requestedRangeCounters);
    }

    private void tryToAssignPendings(Sector sector){

        for(RequestedRangeCounter reqRangeCounter : pendingRequestedCounters.get(sector)){
            RequestedRangeCounter assigned = findSpaceForRange(sector, reqRangeCounter.getFlights(), reqRangeCounter.getAirline(), reqRangeCounter.getRequestedRange());
            if(assigned != null){
                pendingRequestedCounters.get(sector).remove(reqRangeCounter); // If it was assigned, remove it from the queue
                // Notify the counters where assigned
                notifications.notifyCountersAssigned(assigned.getCounterFrom(), assigned.getCounterTo(), sector.getName(), assigned.getFlights(), assigned.getAirline());
            }else{

            }
        }

        // TODO: for each that checks who i need to send a notification that has moved up the queue

    }

    public boolean airlineExists(Airline airline){
        return airlines.contains(airline);
    }

    public boolean flightExists(String flightName){
        return flights.containsKey(new Flight(flightName));
    }

}

