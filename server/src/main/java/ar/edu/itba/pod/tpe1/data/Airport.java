package ar.edu.itba.pod.tpe1.data;

import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.utils.*;
import counter.CounterReservationServiceOuterClass;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        // TODO: Implement condition where if sector has counters (2-4) and first Id is 5, should create a contiguos sector (2-7)
        // And the range of counters to the sector
        sectors.get(sector).add(new RangeCounter(firstId, firstId + count - 1));

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
        return new ConcurrentHashMap<>(sectors);
    }

    // TODO: Needs synchronization
    public List<AssignedRangeCounter> getAssignedRangeCounters(String sectorName, int from, int to) {
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

    public AssignedRangeCounter assignCounters (String sectorName, int count, String airlineName, List<String> flightsToReserve){
        /* Validations:
                - No existe un sector con ese nombre
                - No se agregaron pasajeros esperados con el código de vuelo, para al menos uno de los vuelos solicitados
                - Se agregaron pasajeros esperados con el código de vuelo pero con otra aerolínea, para al menos uno de los vuelos solicitados
                - Ya existe al menos un mostrador asignado para al menos uno de los vuelos solicitados (no se permiten agrandar rangos de mostradores asignados)
                - Ya existe una solicitud pendiente de un rango de mostradores para al menos uno de los vuelos solicitados (no se permiten reiterar asignaciones pendientes)
                - Ya se asignó y luego se liberó un rango de mostradores para al menos uno de los vuelos solicitados (no se puede iniciar el check-in de un vuelo dos o más veces)
        */
        Sector sector = Sector.fromName(sectorName);
        Airline airline = new Airline(airlineName);

        // If the sector does not exist fail
        if(!sectors.containsKey(sector)){
            return null;
        }
        // If at least one of the flights has no expected passengers, or it belongs to another airline, then fail
        for(String flight : flightsToReserve){
            Airline a = flights.getOrDefault(new Flight(flight), null);
            if(a == null || !a.equals(airline)){
                return null;
            }
        }


        return null;
    }

}

