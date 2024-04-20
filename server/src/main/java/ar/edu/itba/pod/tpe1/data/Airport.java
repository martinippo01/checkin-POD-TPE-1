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
        if(sectors.putIfAbsent(new Sector(sectorName), new ArrayList<>()) != null)
            return false;
        // In case it does not exist, create the pending queue for counter assignments
        pendingRequestedCounters.put(new Sector(sectorName), new ConcurrentLinkedQueue<>());
        return true;
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

    public List<CounterServiceOuterClass.CounterInfo> queryCountersBySector(String sectorName) throws RuntimeException {
        if(!sectorName.equals(""))
            return queryCounters(sectorName).stream().map(
                    requestedRangeCounter -> CounterServiceOuterClass.CounterInfo.newBuilder()
                        .setSector(requestedRangeCounter.getCounterFrom() + "-" + requestedRangeCounter.getCounterTo())
                        .setAirline(requestedRangeCounter.getAirline().getName())
                        .addAllFlights(requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).toList())
                        .setWaitingPeople(requestedRangeCounter.getRequestedRange())
                        .setSector(sectorName)
                        .build()
            ).toList();

        List<CounterServiceOuterClass.CounterInfo> out = new ArrayList<>();

        for (Sector sector : sectors.keySet()) {
            out.addAll(
                    queryCounters(sector.getName()).stream().map(
                    requestedRangeCounter -> CounterServiceOuterClass.CounterInfo.newBuilder()
                            .setSector(requestedRangeCounter.getCounterFrom() + "-" + requestedRangeCounter.getCounterTo())
                            .setAirline(requestedRangeCounter.getAirline().getName())
                            .addAllFlights(requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).toList())
                            .setWaitingPeople(requestedRangeCounter.getRequestedRange())
                            .setSector(sectorName)
                            .build()).toList());
        }

        return out;
    }

    public List<RequestedRangeCounter> queryCounters(String sectorName) throws RuntimeException {

        Sector sector = new Sector(sectorName);
        List<RangeCounter> sectorCounters = sectors.getOrDefault(sector, new ArrayList<>());

        if(sectorCounters.isEmpty())
            throw new IllegalArgumentException("No counters found for the specified sector.");

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
            return null;

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
        if(!sectors.containsKey(sector) || to < from)
            return null;

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

    // TODO:
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
        /*
        TODO: Check conditions
        No existe un sector con ese nombre
        No se agregaron pasajeros esperados con el código de vuelo, para al menos uno de los vuelos solicitados
        Se agregaron pasajeros esperados con el código de vuelo pero con otra aerolínea, para al menos uno de los vuelos solicitados
        Ya existe al menos un mostrador asignado para al menos uno de los vuelos solicitados (no se permiten agrandar rangos de mostradores asignados)
        Ya existe una solicitud pendiente de un rango de mostradores para al menos uno de los vuelos solicitados (no se permiten reiterar asignaciones pendientes)
        Ya se asignó y luego se liberó un rango de mostradores para al menos uno de los vuelos solicitados (no se puede iniciar el check-in de un vuelo dos o más veces)
        * */

        // Get the sector
        Sector sector = Sector.fromName(sectorName);
        if (!sectors.containsKey(sector)) {
            // Sector does not exist
            throw new IllegalArgumentException();
        }

        // Check if all flights are valid and linked to the specified airline
        Airline airline = new Airline(airlineName);
        List<Flight> validFlights = new ArrayList<>();
        for (String flightCode : flightsToReserve) {
            Flight flight = new Flight(flightCode);
            Airline registeredAirline = flights.getOrDefault(flight, null);
            if (registeredAirline == null || !registeredAirline.equals(airline)) {
                // If any flight does not exist or is registered to a different airline
                throw new IllegalArgumentException();
            }
            validFlights.add(flight);
        }

        RequestedRangeCounter assigned = findSpaceForRange(sector, validFlights, airline, count);

        if(assigned == null) {
            pendingRequestedCounters.putIfAbsent(sector, new ConcurrentLinkedQueue<>());
            pendingRequestedCounters.get(sector).add(new RequestedRangeCounter(validFlights, airline, true, count));
            return null;
        }
        return assigned;

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
        Queue<RequestedRangeCounter> requestedRangeCounters = pendingRequestedCounters.get(sector);
        if(requestedRangeCounters == null)
            throw new IllegalArgumentException();
        return new ArrayList<>(requestedRangeCounters);
    }

    private void tryToAssignPendings(Sector sector){

        for(RequestedRangeCounter reqRangeCounter : pendingRequestedCounters.get(sector)){
            RequestedRangeCounter assigned = findSpaceForRange(sector, reqRangeCounter.getFlights(), reqRangeCounter.getAirline(), reqRangeCounter.getRequestedRange());
            if(assigned != null){
                pendingRequestedCounters.get(sector).remove(reqRangeCounter); // If it was assigned, remove it from the queue
                // Notify notifications
            }else{

            }
        }

    }

}

