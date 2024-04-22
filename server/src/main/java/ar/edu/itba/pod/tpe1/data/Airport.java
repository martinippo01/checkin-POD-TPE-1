package ar.edu.itba.pod.tpe1.data;

import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.*;
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
    private final ConcurrentHashMap<Booking, Flight> bookingCodes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Flight, Airline> flights = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Booking, CheckIn> checkIns = new ConcurrentHashMap<>();

    // Key: Sector - Value: A list of range of sectors
    private final ConcurrentHashMap<Sector, List<RangeCounter>> sectors = new ConcurrentHashMap<>();


    private final AtomicInteger counterId = new AtomicInteger(1);

    private static Airport instance = null;


    private final Map<Sector, Queue<RequestedRangeCounter>> pendingRequestedCounters = new ConcurrentHashMap<>();

    private Airport() {
    }

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
        if (sectors.putIfAbsent(new Sector(sectorName), new ArrayList<>()) != null)
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
        for (RangeCounter rangeCounter : sectors.get(sector)) {
            if (rangeCounter.getCounterTo() >= lastCounterOfSector) {
                lastCounterOfSector = rangeCounter.getCounterTo();
                removeRangeCounter = rangeCounter;
            }
            lastCounterOfSector = Math.max(rangeCounter.getCounterTo(), lastCounterOfSector);
        }
        if (lastCounterOfSector == firstId - 1) {
            sectors.get(sector).remove(removeRangeCounter);
            RangeCounter newRangeCounter = new RangeCounter(removeRangeCounter.getCounterFrom(), firstId + count - 1);
            sectors.get(sector).add(newRangeCounter);
            // Make sure the elements are in the correct order
            Collections.sort(sectors.get(sector));
            return newRangeCounter;
        } else {
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
        Booking booking = new Booking(bookingCode);

        if (bookingCodes.containsKey(booking)) { // In case the booking already exists, it fails
            return false;
        }

        // In case the flight exists
        if (flights.containsKey(flight)) {
            // Check if it belongs to other airline, in that case it fails
            if (!flights.get(flight).equals(airline))
                return false;
        }

        // If absent, put the flight
        flights.putIfAbsent(flight, airline);
        // Put the new booking code
        bookingCodes.put(booking, flight);
        checkIns.put(booking, new CheckIn(CheckInStatus.UNDEFINED, flight));

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
        for (RangeCounter rangeCounter : sectorCounters) {
            for (RequestedRangeCounter counter : rangeCounter.getAssignedRangeCounters()) {
                if (counter.getCounterFrom() <= from && counter.getCounterTo() >= to) {
                    out.add(new RequestedRangeCounter(counter));
                }
            }
        }
        return out;
    }

    // TODO:
    public FreeCounterResult freeCounters(String sectorName, int fromVal, String airlineName) throws CounterReleaseException {

        Sector sector = new Sector(sectorName);
        Airline airline = new Airline(airlineName);
        List<RangeCounter> sectorCounters = sectors.get(sector);

        if (sectorCounters == null) {
            throw new CounterReleaseException("Sector '" + sector + "' does not exist.");
        }

        RequestedRangeCounter rangeCounterFound = null;
        for (RangeCounter rangeCounter : sectorCounters) {
            if (rangeCounter.getCounterFrom() <= fromVal && rangeCounter.getCounterTo() >= fromVal) {
                RequestedRangeCounter temp = rangeCounter.freeRange(fromVal, airline);
                if (temp != null) {
                    rangeCounterFound = temp;
                    break;
                }

            }
        }

        if (rangeCounterFound == null) {
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

        // Attempt to find a contiguous block of counters
        List<RangeCounter> ranges = sectors.get(sector);
        RequestedRangeCounter assignedRangeCounter = null;
        for (RangeCounter rangeCounter : ranges) {
            assignedRangeCounter = rangeCounter.assignRange(count, validFlights, airline);
            if (assignedRangeCounter != null) {
                // TODO: Check that this updates requestedCounters in each Flight in airport
                airline.addRequestedCounters(validFlights, assignedRangeCounter);
                return assignedRangeCounter;
            }
        }

        pendingRequestedCounters.putIfAbsent(sector, new ConcurrentLinkedQueue<>());
        pendingRequestedCounters.get(sector).add(new RequestedRangeCounter(validFlights, airline, true, count));
        return null;
    }

    public List<RequestedRangeCounter> listPendingRequestedCounters(String sectorName) {
        Sector sector = new Sector(sectorName);
        Queue<RequestedRangeCounter> requestedRangeCounters = pendingRequestedCounters.get(sector);
        if (requestedRangeCounters == null)
            throw new IllegalArgumentException();
        return new ArrayList<>(requestedRangeCounters);
    }

    private RequestedRangeCounter rangeCounterBySector(Sector sector, int counter) {
        Objects.requireNonNull(sector, "Received NULL sector");

        for (RangeCounter rangeCounter : sectors.get(sector)) {
            Optional<RequestedRangeCounter> rrc = rangeCounter.getAssignedRangeCounterWithCounter(counter);
            if (rrc.isPresent()) {
                return rrc.get();
            }
        }

        return null;
    }

    public boolean performCheckIn(String sectorName, int counterFrom, String airlineName) {
        Sector sector = new Sector(sectorName);
        if (!sectors.containsKey(sector)) {
            return false; // Sector does not exist
        }

        RequestedRangeCounter requestedRangeCounter = rangeCounterBySector(sector, counterFrom);
        if (requestedRangeCounter == null) {
            return false; // Counter not in range/sector
        }

        if (!requestedRangeCounter.getAirline().getName().equals(airlineName)) {
            return false; // Invalid airline name
        }

        int queueLength = requestedRangeCounter.getWaitingQueueLength();
        if (queueLength == 0) {
            return true; // Early return: nothing to do
        }

        int checkInsToPerform = Math.min(queueLength, requestedRangeCounter.getSize());

        List<Booking> checkedInBookings = new ArrayList<>(checkInsToPerform);
        for (int i = 0; i < checkInsToPerform; i++) {
            Booking pendingCheckIn = requestedRangeCounter.getFromWaitingQueue();
            CheckIn queuedCheckIn = checkIns.get(pendingCheckIn);

            if (!requestedRangeCounter.getFlights().contains(queuedCheckIn.getFlight())) {
                return false; // Flight mismatch
            }

            checkIns.replace(
                    pendingCheckIn,
                    queuedCheckIn,
                    new CheckIn(
                            CheckInStatus.DONE,
                            queuedCheckIn.getFlight(),
                            requestedRangeCounter.getCounterFrom() + i,
                            sector));

            checkedInBookings.add(pendingCheckIn);
        }

        // TODO: Return check ins from checked in bookings
        return true;
    }

    private List<CountersInformation> getRangeCountersAsInformation(Flight flight, Airline airline, Set<RequestedRangeCounter> requestedRangeCounters) {
        List<CountersInformation> rangeCountersAsInformation = new ArrayList<>(requestedRangeCounters.size());
        requestedRangeCounters.forEach(
                rrc -> {
                    rangeCountersAsInformation.add(CountersInformation.newBuilder()
                            .setCounters(CounterRange.newBuilder()
                                    .setFirstCounterNumber(rrc.getCounterFrom())
                                    .setNumberOfConsecutiveCounters(rrc.getSize())
                                    .build())
                            .setSectorName("") // TODO
                            .setPeopleInQueue(rrc.getWaitingQueueLength())
                            .build()
                    );
                }
        );

        return rangeCountersAsInformation;
    }

    public FetchCounterResponse.Builder listAssignedCounters(String bookingCode) {
        FetchCounterResponse.Builder response = FetchCounterResponse.newBuilder();

        Booking booking = new Booking(bookingCode);
        if (!bookingCodes.containsKey(booking)) {
            response.setStatus(CounterStatus.COUNTER_STATUS_BOOKING_CODE_WITHOUT_AWAITING_PASSENGERS);
            return response;
        }

        Flight flight = bookingCodes.get(booking);
        Airline airline = flights.get(flight);

        response.setBooking(
                BookingInformation.newBuilder()
                        .setBookingCode(bookingCode)
                        .setFlightCode(flight.getFlightCode())
                        .setAirlineName(airline.getName())
                        .build()
        );


        Set<RequestedRangeCounter> requestedRangeCounters = airline.getRequestedCounters(flight);
        if (requestedRangeCounters.isEmpty()) {
            return response.setStatus(CounterStatus.COUNTER_STATUS_COUNTERS_NOT_ASSIGNED);
        }

        List<CountersInformation> rangeCountersAsInformation = getRangeCountersAsInformation(flight, airline, requestedRangeCounters);

        return response.setStatus(CounterStatus.COUNTER_STATUS_COUNTERS_ASSIGNED)
                .addAllData(rangeCountersAsInformation);
    }

    public PassengerCheckinResponse.Builder addToCheckInQueue(String bookingCode, String sectorName, int counterNumber) {
        PassengerCheckinResponse.Builder response = PassengerCheckinResponse.newBuilder();

        Booking booking = new Booking(bookingCode);
        if (!bookingCodes.containsKey(booking)) {
            return response.setStatus(CheckinStatus.CHECKIN_STATUS_INVALID_BOOKING_CODE);
        }

        Flight flight = bookingCodes.get(booking);

        response.setBooking(BookingInformation.newBuilder()
                .setBookingCode(bookingCode)
                .setAirlineName(flights.get(flight).getName())
                .setFlightCode(flight.getFlightCode()).build());

        Sector sector = new Sector(sectorName);
        if (!sectors.containsKey(sector)) {
            return response.setStatus(CheckinStatus.CHECKIN_STATUS_INVALID_SECTOR_ID);
        }

        RequestedRangeCounter requestedRangeCounter = rangeCounterBySector(sector, counterNumber);
        if (requestedRangeCounter == null) {
            return response.setStatus(CheckinStatus.CHECKIN_STATUS_INVALID_COUNTER_NUMBER);
        }
        if (!requestedRangeCounter.getFlights().contains(flight)) {
            return response.setStatus(CheckinStatus.CHECKIN_STATUS_INVALID_FLIGHT_COUNTER_NUMBER);
        }

        CheckIn currentCheckIn = checkIns.get(booking);
        if (currentCheckIn.getStatus().equals(CheckInStatus.QUEUE)) {
            // Already queued
            return response.setStatus(CheckinStatus.CHECKIN_STATUS_PASSENGER_ALREADY_IN_QUEUE)
                    .setData(
                            CountersInformation.newBuilder()
                                    .setCounters(CounterRange.newBuilder()
                                            .setFirstCounterNumber(requestedRangeCounter.getCounterFrom())
                                            .setNumberOfConsecutiveCounters(requestedRangeCounter.getSize())
                                            .build())
                                    .setSectorName(currentCheckIn.getSector().getName())
                                    .setPeopleInQueue(requestedRangeCounter.getWaitingQueueLength())
                                    .build()
                    );
        } else if (currentCheckIn.getStatus().equals(CheckInStatus.DONE)) {
            return response.setStatus(CheckinStatus.CHECKIN_STATUS_CHECKIN_ALREADY_DONE)
                    .setData(
                            CountersInformation.newBuilder()
                                    .setCounters(CounterRange.newBuilder()
                                            .setFirstCounterNumber(requestedRangeCounter.getCounterFrom())
                                            .setNumberOfConsecutiveCounters(requestedRangeCounter.getSize())
                                            .build())
                                    .setSectorName(currentCheckIn.getSector().getName())
                                    .setPeopleInQueue(requestedRangeCounter.getWaitingQueueLength())
                                    .build()
                    );
        }

        checkIns.replace(booking, currentCheckIn, new CheckIn(CheckInStatus.QUEUE, flight, requestedRangeCounter, sector));

        requestedRangeCounter.addBookingToWaitingQueue(booking);

        return response.setStatus(CheckinStatus.CHECKIN_STATUS_ADDED_TO_QUEUE)
                .setData(
                        CountersInformation.newBuilder()
                                .setCounters(CounterRange.newBuilder()
                                        .setFirstCounterNumber(requestedRangeCounter.getCounterFrom())
                                        .setNumberOfConsecutiveCounters(requestedRangeCounter.getSize())
                                        .build())
                                .setSectorName(sector.getName())
                                .setPeopleInQueue(requestedRangeCounter.getWaitingQueueLength())
                                .build()
                );
    }

    public PassengerStatusResponse.Builder getCheckIn(String bookingCode) {
        PassengerStatusResponse.Builder response = PassengerStatusResponse.newBuilder();

        Booking booking = new Booking(bookingCode);
        if (!bookingCodes.containsKey(booking)) {
            return response.setStatus(PassengerStatus.PASSENGER_STATUS_INVALID_BOOKING_CODE);
        }

        Flight flight = bookingCodes.get(booking);
        Airline airline = flights.get(flight);
        response.setBooking(BookingInformation.newBuilder()
                .setBookingCode(bookingCode)
                .setFlightCode(flight.getFlightCode())
                .setAirlineName(airline.getName())
                .build());

        if (!checkIns.containsKey(booking)) {
            return response.setStatus(PassengerStatus.PASSENGER_STATUS_UNDEFINED);
        }

        CheckIn checkIn = checkIns.get(booking);
        if (checkIn.getStatus().equals(CheckInStatus.DONE)) {
            return response.setStatus(PassengerStatus.PASSENGER_STATUS_CHECKIN_ALREADY_DONE)
                    .addData(
                            PassengerStatusData.newBuilder().setCheckedInCounter(
                                    CounterInformation.newBuilder()
                                            .setCounter(checkIn.getCounterWhereCheckInWasDone())
                                            .setSectorName(checkIn.getSector().getName())
                                            .build()
                            ).build());
        } else if (checkIn.getStatus().equals(CheckInStatus.QUEUE)) {
            return response.setStatus(PassengerStatus.PASSENGER_STATUS_WAITING_FOR_CHECKIN)
                    .addData(
                            PassengerStatusData.newBuilder().setAvailableCounters(
                                    CountersInformation.newBuilder()
                                            .setCounters(CounterRange.newBuilder()
                                                    .setFirstCounterNumber(checkIn.getRangeCounter().getCounterFrom())
                                                    .setNumberOfConsecutiveCounters(checkIn.getRangeCounter().getSize())
                                                    .build())
                                            .setSectorName(checkIn.getSector().getName())
                                            .setPeopleInQueue(checkIn.getRangeCounter().getWaitingQueueLength())
                            ).build());
        }

        Set<RequestedRangeCounter> requestedRangeCounters = airline.getRequestedCounters(flight);
        if (requestedRangeCounters.isEmpty()) {
            return response.setStatus(PassengerStatus.PASSENGER_STATUS_COUNTERS_NOT_ASSIGNED);
        }

        List<CountersInformation> rangeCountersAsInformation = getRangeCountersAsInformation(flight, airline, requestedRangeCounters);

        return response.setStatus(PassengerStatus.PASSENGER_STATUS_OUT_OF_QUEUE)
                .addAllData(
                        rangeCountersAsInformation.stream().map(
                                rci -> PassengerStatusData.newBuilder()
                                        .setAvailableCounters(rci)
                                        .build()
                        ).toList()
                );
    }
}
