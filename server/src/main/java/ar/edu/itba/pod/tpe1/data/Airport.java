package ar.edu.itba.pod.tpe1.data;

import ar.edu.itba.pod.tpe1.data.utils.*;
import ar.edu.itba.pod.tpe1.protos.CheckInService.*;
import ar.edu.itba.pod.tpe1.protos.CounterService.CheckInRecord;
import ar.edu.itba.pod.tpe1.protos.CounterService.CounterInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Airport {

    private final Notifications notifications = Notifications.getInstance();

    private final Object lock = "AIRPORT_LOCK";

    // Key: Booking - Value: a boolean
    private final ConcurrentHashMap<Booking, Flight> bookingCodes = new ConcurrentHashMap<>();

    // Key: Flight - Value: An airline that
    private final ConcurrentHashMap<Flight, Airline> flights = new ConcurrentHashMap<>();

    // Every checkin, where CheckIn contains the status of the check in info.
    private final ConcurrentHashMap<Booking, CheckIn> checkIns = new ConcurrentHashMap<>();

    private final Set<Airline> airlines = Collections.synchronizedSet(new HashSet<>());

    // Key: Sector - Value: A list of range of sectors
    private final ConcurrentHashMap<Sector, List<RangeCounter>> sectors = new ConcurrentHashMap<>();


    private final ConcurrentHashMap<Flight, Boolean> flightWasAssigned = new ConcurrentHashMap<>();

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

    public void addSector(String sectorName) throws Exception {
        Sector sector = new Sector(sectorName);
        synchronized (lock) {
            if (sectors.putIfAbsent(sector, new ArrayList<>()) != null)
                throw new IllegalArgumentException("Sector " + sectorName + " already exists");

            pendingRequestedCounters.put(sector, new ConcurrentLinkedQueue<>());
        }
    }

    public RangeCounter addCounters(String sectorName, int count) {

        Sector sector = Sector.fromName(sectorName);
        synchronized (lock) {
            if (count <= 0)
                throw new IllegalArgumentException("Invalid number of counters (must be positive).");
            if (!sectors.containsKey(sector))
                throw new IllegalArgumentException("Sector " + sectorName + " does not exist.");
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
                RangeCounter newRangeCounter = new RangeCounter(removeRangeCounter, firstId + count - 1);
                sectors.get(sector).add(newRangeCounter);
                // Make sure the elements are in the correct order
                Collections.sort(sectors.get(sector));
                tryToAssignPendings(sector); // If there were pending assignments, try to solve them
                return newRangeCounter;
            } else {
                RangeCounter newRangeCounter = new RangeCounter(firstId, firstId + count - 1);
                // And the range of counters to the sector, from the last
                sectors.get(sector).add(newRangeCounter);
                // Make sure the elements are in the correct order
                Collections.sort(sectors.get(sector));
                tryToAssignPendings(sector); // If there were pending assignments, try to solve them
                return newRangeCounter; // Success, returns the first ID of the new counters
            }
        }
    }

    // Register a passenger, link booking and flight codes
    public void registerPassenger(String bookingCode, String flightCode, String airlineName) throws Exception {

        Flight flight = new Flight(flightCode);
        Airline airline = new Airline(airlineName);
        Booking booking = new Booking(bookingCode);

        synchronized (lock) {
            if (bookingCodes.containsKey(booking)) { // In case the booking already exists, it fails
                throw new IllegalArgumentException("Booking code " + bookingCode + " already exists.");
            }

            // In case the flight exists
            if (flights.containsKey(flight)) {
                // Check if it belongs to other airline, in that case it fails
                if (!flights.get(flight).equals(airline))
                    throw new IllegalArgumentException("The flight " + flightCode + " is already registered to airline " + flights.get(flight) + ".");
            }

            // If absent, put the flight and mark as it is not assigned yet
            flights.putIfAbsent(flight, airline);
            flightWasAssigned.putIfAbsent(flight, false);
            airlines.add(airline);
            // Put the new booking code
            bookingCodes.put(booking, flight);
            checkIns.put(booking, new CheckIn(CheckInStatus.UNDEFINED, flight));
        }
    }

    public List<CounterInfo> queryCountersBySector(String sectorName) throws RuntimeException {
        //This
        if (sectors.isEmpty()) {
            throw new IllegalStateException("There are no sectors registered at the airport.");
        }
        List<CounterInfo> out = new ArrayList<>();
        synchronized (lock) {
            // In case there's no specified sector, print every sector and then return
            if (!sectorName.isEmpty()) {
                return queryCounters(sectorName, false).stream().map(
                        requestedRangeCounter -> CounterInfo.newBuilder()
                                .setSector(requestedRangeCounter.getCounterFrom() + "-" + requestedRangeCounter.getCounterTo())
                                .setAirline(requestedRangeCounter.getAirline().getName())
                                .addAllFlights(requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).toList())
                                .setWaitingPeople(requestedRangeCounter.getRequestedRange())
                                .setSector(sectorName)
                                .build()
                ).toList();
            }
            // Otherwise, print the counters for the specified sector and return
            for (Sector sector : sectors.keySet()) {
                String sectorName2 = sector.getName();
                List<RequestedRangeCounter> requestedRangeCounters = queryCounters(sectorName2, true);

                for (RequestedRangeCounter requestedRangeCounter : requestedRangeCounters) {
                    CounterInfo counterInfo = CounterInfo.newBuilder()
                            .setSector(requestedRangeCounter.getCounterFrom() + "-" + requestedRangeCounter.getCounterTo())
                            .setAirline(requestedRangeCounter.getAirline().getName())
                            .addAllFlights(requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).toList())
                            .setWaitingPeople(requestedRangeCounter.getRequestedRange())
                            .setSector(sector.getName())
                            .build();
                    out.add(counterInfo);
                }
            }
        }

        return out;
    }

    private List<RequestedRangeCounter> queryCounters(String sectorName, Boolean allCounters) throws RuntimeException {

        Sector sector = new Sector(sectorName);
        List<RequestedRangeCounter> out = new ArrayList<>();
        boolean containsAssignedRangeCounter = false;

        synchronized (lock) {
            List<RangeCounter> sectorCounters = sectors.getOrDefault(sector, new ArrayList<>());

            //a small patch, really hard bug to catch
            if (sectorCounters.isEmpty() && !allCounters) {
                throw new IllegalArgumentException("No counters found for the specified sector.");
            }


            for (RangeCounter rangeCounter : sectorCounters) {
                int prevFrom = rangeCounter.getCounterFrom();
                for (RequestedRangeCounter counter : rangeCounter.getAssignedRangeCounters()) {
                    if (prevFrom < counter.getCounterFrom())
                        out.add(new RequestedRangeCounter(prevFrom, counter.getCounterFrom() - 1, new ArrayList<>(), new Airline(""), false, sector));
                    containsAssignedRangeCounter = true;
                    out.add(new RequestedRangeCounter(counter));
                    prevFrom = counter.getCounterTo() + 1;
                }
                if (prevFrom <= rangeCounter.getCounterTo())
                    out.add(new RequestedRangeCounter(prevFrom, rangeCounter.getCounterTo(), new ArrayList<>(), new Airline(""), false, sector));
            }
        }
        return containsAssignedRangeCounter ? out : new ArrayList<>();
    }

    public List<CheckInRecord> queryCheckIns(String sector, String airline) {
        // TODO: Test
        List<CheckInRecord> out = new ArrayList<>();
        // Check that there's at least one checkin with status DONE
        synchronized (lock) {
            Iterator<Booking> bookingIterator = checkIns.keySet().iterator();
            boolean thereCheckIns = false;
            while (bookingIterator.hasNext() && !thereCheckIns) {
                Booking booking = bookingIterator.next();
                if (checkIns.get(booking).getStatus() == CheckInStatus.DONE)
                    thereCheckIns = true;
            }
            if (!thereCheckIns)
                throw new IllegalArgumentException("No check-ins have been made yet");


            boolean filterBySector = !sector.isEmpty();
            boolean filterByAirline = !airline.isEmpty();

            for (Booking booking : checkIns.keySet()) {
                CheckInRecord.Builder checkInRecordBuilder = CheckInRecord.newBuilder();
                CheckIn checkIn = checkIns.get(booking);
                // To add to the list should be a DONE checkin, also match the airline (if specified) nad match the sector (if specified)
                // Remember property A => B <=> !A or B
                if (checkIn.getStatus() == CheckInStatus.DONE // CheckIn is DONE
                        && (!filterByAirline || airline.equals(flights.get(checkIn.getFlight()).getName())) // If specified, checkin belongs to the same Airline
                        && (!filterBySector || sector.equals(checkIn.getSector().getName())) // If specified, checkin belongs to the same Sector
                ) {
                    out.add(checkInRecordBuilder
                            .setBookingCode(booking.getBookingCode())
                            .setAirline(flights.get(checkIn.getFlight()).getName())
                            .setCounter(checkIn.getCounterWhereCheckInWasDone())
                            .setSector(checkIn.getSector().getName())
                            .setFlight(checkIn.getFlight().getFlightCode())
                            .build()
                    );
                }
            }
        }


        return out;
    }

    public Map<Sector, List<RangeCounter>> getSectors() {

        if (sectors.isEmpty())
            throw new IllegalStateException("There are no sectors registered at the airport.");

        Map<Sector, List<RangeCounter>> toReturn;
        synchronized (lock) {
            toReturn = new ConcurrentHashMap<>(sectors);
        }
        return toReturn;
    }

    public List<RequestedRangeCounter> listCounters(String sectorName, int from, int to) {

        Sector sector = new Sector(sectorName);
        // If sector does not exist or range is not valid, fail
        if (!sectors.containsKey(sector))
            throw new IllegalStateException("Invalid sector");
        if (to < from)
            throw new IllegalArgumentException("Invalid range.");


        List<RequestedRangeCounter> out = new ArrayList<>();
        boolean containsAssignedRangeCounter = false;
        synchronized (lock) {
            List<RangeCounter> sectorCounters = sectors.get(sector);

            for (RangeCounter rangeCounter : sectorCounters) {
                if (!(to <= rangeCounter.getCounterFrom() || from >= rangeCounter.getCounterTo())) { // In case the range is outside the from-to
                    int prevFrom = rangeCounter.getCounterFrom();
                    for (RequestedRangeCounter counter : rangeCounter.getAssignedRangeCounters()) {
                        if (prevFrom < counter.getCounterFrom())
                            out.add(new RequestedRangeCounter(prevFrom, counter.getCounterFrom() - 1, new ArrayList<>(), new Airline(""), false, sector));
                        if (counter.getCounterFrom() >= from && counter.getCounterTo() <= to) {
                            containsAssignedRangeCounter = true;
                            out.add(new RequestedRangeCounter(counter));
                        }
                        prevFrom = counter.getCounterTo() + 1;
                    }
                    if (prevFrom <= rangeCounter.getCounterTo())
                        out.add(new RequestedRangeCounter(prevFrom, rangeCounter.getCounterTo(), new ArrayList<>(), new Airline(""), false, sector));
                }
            }
        }
        return containsAssignedRangeCounter ? out : new ArrayList<>();
    }

    public FreeCounterResult freeCounters(String sectorName, int fromVal, String airlineName) throws Exception {

        Sector sector = new Sector(sectorName);
        Airline airline = new Airline(airlineName);
        RequestedRangeCounter rangeCounterFound = null;

        synchronized (lock) {
            List<RangeCounter> sectorCounters = sectors.getOrDefault(sector, new ArrayList<>());

            if (sectorCounters.isEmpty()) {
                throw new ClassNotFoundException("Sector '" + sectorName + "' does not exist.");
            }


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
                throw new IllegalArgumentException("No range starting at counter " + fromVal + " exists in sector '" + sectorName + "'.");
            } else {
                // Notify the airline, tha counters where removed
                notifications.notifyCountersRemoved(rangeCounterFound.getAirline(), rangeCounterFound.getFlights(), rangeCounterFound.getCounterFrom(), rangeCounterFound.getCounterTo(), sectorName);
            }


            if (rangeCounterFound.getWaitingQueueLength() != 0)
                throw new IllegalStateException("Cannot free counters as there are passengers waiting to be attended.");

            // Attempt to assign from the queue when a sector was freed
            tryToAssignPendings(sector);
        }

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
        synchronized (lock) {
            // Validate that the flights are correct
            List<Flight> validFlights = checkFlights(sector, airline, flightsToReserve);

            // In case at least one of the flights is not valid for any reason, fail
            if (validFlights == null)
                throw new IllegalArgumentException();

            // Attempt to assign a range at the requested sector
            RequestedRangeCounter assigned = findSpaceForRange(sector, validFlights, airline, count);

            // In case there was no space for the range, append it to the queue
            if (assigned == null) {
                pendingRequestedCounters.putIfAbsent(sector, new ConcurrentLinkedQueue<>());
                pendingRequestedCounters.get(sector).add(new RequestedRangeCounter(validFlights, airline, true, count, sector));
                notifications.notifyCountersPending(airline, count, sectorName, validFlights, 0); // TODO: send proper pending ahead
                return null;
            } else {
                airline.addRequestedCounters(validFlights, assigned);
                validFlights.forEach(flight -> flights.replace(flight, airline));

                notifications.notifyCountersAssigned(assigned.getCounterFrom(), assigned.getCounterTo(), sectorName, assigned.getFlights(), airline);
            }
            return assigned;
        }
    }

    private List<Flight> checkFlights(Sector sector, Airline airline, List<String> flightsToReserve) {
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
            if (registeredAirline == null)
                throw new IllegalArgumentException("Airline '" + airline + "' does not exist (for flight '" + flightCode + "').");
            if (!registeredAirline.equals(airline))
                throw new IllegalArgumentException("Flight '" + flightCode + "' does not match Airline '" + registeredAirline + "'.");
            if (flightWasAssigned.get(flight))
                throw new IllegalArgumentException("The flight '" + flightCode + "' is already assigned.");


            // For each sector of the airport
            for (Sector otherSector : sectors.keySet()) {
                // Check that the flight is not assigned in another counter
                for (RangeCounter rangeCounter : sectors.getOrDefault(otherSector, new ArrayList<>())) {
                    for (RequestedRangeCounter counter : rangeCounter.getAssignedRangeCounters()) {
                        if (counter.getFlights().contains(flight))
                            throw new IllegalArgumentException("The flight '" + flightCode + "' is already assigned at sector " + otherSector.getName() + ".");
                    }
                }
                // Check that there is no pending assignation with the flight
                for (RequestedRangeCounter pendingAssignation : pendingRequestedCounters.get(sector)) {
                    if (pendingAssignation.getFlights().contains(flight))
                        throw new IllegalArgumentException("The flight '" + flightCode + "' is pending to be assigned.");
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

    private RequestedRangeCounter findSpaceForRange(Sector sector, List<Flight> flights, Airline airline, int count) {
        List<RangeCounter> ranges = sectors.get(sector);
        RequestedRangeCounter assignedRangeCounter = null;
        for (RangeCounter rangeCounter : ranges) {
            assignedRangeCounter = rangeCounter.assignRange(count, flights, airline, sector);
            if (assignedRangeCounter != null) {
                return assignedRangeCounter;
            }
        }
        return null;
    }

    public List<RequestedRangeCounter> listPendingRequestedCounters(String sectorName) {
        Sector sector = new Sector(sectorName);
        Queue<RequestedRangeCounter> requestedRangeCounters = pendingRequestedCounters.getOrDefault(sector, new ArrayDeque<>());
        if (requestedRangeCounters.isEmpty())
            throw new IllegalArgumentException("Invalid sector.");
        return new ArrayList<>(requestedRangeCounters);
    }

    private void tryToAssignPendings(Sector sector) {

        for (RequestedRangeCounter reqRangeCounter : pendingRequestedCounters.get(sector)) {
            RequestedRangeCounter assigned = findSpaceForRange(sector, reqRangeCounter.getFlights(), reqRangeCounter.getAirline(), reqRangeCounter.getRequestedRange());
            if (assigned != null) {
                pendingRequestedCounters.get(sector).remove(reqRangeCounter); // If it was assigned, remove it from the queue
                // Notify the counters where assigned
                notifications.notifyCountersAssigned(assigned.getCounterFrom(), assigned.getCounterTo(), sector.getName(), assigned.getFlights(), assigned.getAirline());
            } else {
                // TODO: aca??
            }
        }

        // TODO: for each that checks who i need to send a notification that has moved up the queue

    }

    public boolean airlineExists(Airline airline) {
        return airlines.contains(airline);
    }

    public boolean flightExists(String flightName) {
        return flights.containsKey(new Flight(flightName));
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

    public CheckInCountersResponse.Builder performCheckIn(String sectorName, int counterFrom, String airlineName) throws Exception {
        CheckInCountersResponse.Builder response = CheckInCountersResponse.newBuilder();

        Sector sector = new Sector(sectorName);
        if (!sectors.containsKey(sector)) {
            throw new IllegalArgumentException("Invalid sector name.");
        }
        List<CheckInCounterInformation> checkInCountersInformation = new ArrayList<>();
        synchronized (lock) {
            RequestedRangeCounter requestedRangeCounter = rangeCounterBySector(sector, counterFrom);
            if (requestedRangeCounter == null) {
                throw new IllegalArgumentException("Invalid counter number.");
            }

            if (!requestedRangeCounter.getAirline().getName().equals(airlineName)) {
                throw new IllegalArgumentException("Invalid airline name.");
            }

            int queueLength = requestedRangeCounter.getWaitingQueueLength();

            int checkInsToPerform = Math.min(queueLength, requestedRangeCounter.getSize());


            for (int i = 0; i < checkInsToPerform; i++) {
                Booking pendingCheckIn = requestedRangeCounter.getFromWaitingQueue();
                CheckIn queuedCheckIn = checkIns.get(pendingCheckIn);

                if (!requestedRangeCounter.getFlights().contains(queuedCheckIn.getFlight())) {
                    continue; // Flight mismatch
                }

                CheckIn performedCheckIn = new CheckIn(
                        CheckInStatus.DONE,
                        queuedCheckIn.getFlight(),
                        requestedRangeCounter.getCounterFrom() + i,
                        sector);

                checkIns.replace(
                        pendingCheckIn,
                        queuedCheckIn,
                        performedCheckIn);


                notifications.notifyCheckIn(
                        flights.get(performedCheckIn.getFlight()),
                        pendingCheckIn.getBookingCode(),
                        performedCheckIn.getFlight().getFlightCode(),
                        performedCheckIn.getCounterWhereCheckInWasDone()
                );

                checkInCountersInformation.add(
                        CheckInCounterInformation.newBuilder()
                                .setStatus(CheckInCounterStatus.CHECK_IN_COUNTER_STATUS_SUCCESS)
                                .setBooking(BookingInformation.newBuilder()
                                        .setBookingCode(pendingCheckIn.getBookingCode())
                                        .setFlightCode(performedCheckIn.getFlight().getFlightCode())
                                        .setAirlineName(flights.get(performedCheckIn.getFlight()).getName())
                                )
                                .setCounter(performedCheckIn.getCounterWhereCheckInWasDone())
                                .build()
                );
            }

            // Some counters didn't perform a check-in
            if (checkInCountersInformation.size() < requestedRangeCounter.getSize()) {
                final int lastCounterWithCheckIn;
                if (checkInCountersInformation.isEmpty()) { // In case there were no check-ins at all
                    lastCounterWithCheckIn = 0;
                } else {
                    lastCounterWithCheckIn = checkInCountersInformation.get(checkInCountersInformation.size() - 1).getCounter();
                }
                final int lastIdleCounter = requestedRangeCounter.getCounterTo() - lastCounterWithCheckIn + 1;

                for (int i = lastCounterWithCheckIn; i < lastIdleCounter; i++) {
                    checkInCountersInformation.add(
                            CheckInCounterInformation.newBuilder()
                                    .setStatus(CheckInCounterStatus.CHECK_IN_COUNTER_STATUS_IDLE)
                                    .setCounter(i + 1)
                                    .build()
                    );
                }
            }
        }

        return response.setStatus(CheckInCountersStatus.CHECK_IN_COUNTERS_STATUS_CHECKIN_DONE).addAllData(checkInCountersInformation);
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
                            .setSectorName(rrc.getSector().getName())
                            .setPeopleInQueue(rrc.getWaitingQueueLength())
                            .build()
                    );
                }
        );

        return rangeCountersAsInformation;
    }

    public FetchCounterResponse.Builder listAssignedCounters(String bookingCode) throws Exception {
        FetchCounterResponse.Builder response = FetchCounterResponse.newBuilder();

        Booking booking = new Booking(bookingCode);

        if (!bookingCodes.containsKey(booking)) {
            throw new IllegalArgumentException("Invalid booking code.");
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
        Set<RequestedRangeCounter> requestedRangeCounters;
        // TODO: Check if it really needs lock
        synchronized (lock) {
            requestedRangeCounters = airline.getRequestedCounters(flight);
            if (requestedRangeCounters == null || requestedRangeCounters.isEmpty()) {
                throw new IllegalStateException("No counters assigned for the flight.");
            }

            List<CountersInformation> rangeCountersAsInformation = getRangeCountersAsInformation(flight, airline, requestedRangeCounters);
            return response.setStatus(CounterStatus.COUNTER_STATUS_COUNTERS_ASSIGNED)
                    .addAllData(rangeCountersAsInformation);
        }
    }

    public PassengerCheckinResponse.Builder addToCheckInQueue(String bookingCode, String sectorName, int counterNumber) throws Exception {
        PassengerCheckinResponse.Builder response = PassengerCheckinResponse.newBuilder();

        Booking booking = new Booking(bookingCode);
        if (!bookingCodes.containsKey(booking)) {
            throw new IllegalArgumentException("Invalid booking code.");
        }

        Flight flight = bookingCodes.get(booking);

        response.setBooking(BookingInformation.newBuilder()
                .setBookingCode(bookingCode)
                .setAirlineName(flights.get(flight).getName())
                .setFlightCode(flight.getFlightCode()).build());

        Sector sector = new Sector(sectorName);
        if (!sectors.containsKey(sector)) {
            throw new IllegalArgumentException("Invalid sector name.");
        }

        synchronized (lock) {
            RequestedRangeCounter requestedRangeCounter = rangeCounterBySector(sector, counterNumber);
            if (requestedRangeCounter == null) {
                throw new IllegalArgumentException("Invalid counter number.");
            }
            if (!requestedRangeCounter.getFlights().contains(flight)) {
                throw new IllegalArgumentException("Invalid flight for the counter number.");
            }

            CheckIn currentCheckIn = checkIns.get(booking);
            if (currentCheckIn.getStatus().equals(CheckInStatus.QUEUE)) {
                // Already queued
                throw new IllegalStateException("Passenger already in queue.");
            } else if (currentCheckIn.getStatus().equals(CheckInStatus.DONE)) {
                throw new IllegalStateException("Passenger already checked in.");
            }

            checkIns.replace(booking, currentCheckIn, new CheckIn(CheckInStatus.QUEUE, flight, requestedRangeCounter, sector));

            requestedRangeCounter.addBookingToWaitingQueue(booking);

            notifications.notifyPassengerEnteredQueue(
                    flights.get(flight),
                    booking.getBookingCode(),
                    flight.getFlightCode(),
                    requestedRangeCounter.getCounterFrom(),
                    requestedRangeCounter.getCounterTo(),
                    sector.getName()
            );

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
    }

    public PassengerStatusResponse.Builder getCheckIn(String bookingCode) throws Exception {
        PassengerStatusResponse.Builder response = PassengerStatusResponse.newBuilder();

        Booking booking = new Booking(bookingCode);
        if (!bookingCodes.containsKey(booking)) {
            throw new IllegalArgumentException("Invalid booking code.");
        }

        Flight flight = bookingCodes.get(booking);
        Airline airline = flights.get(flight);
        response.setBooking(BookingInformation.newBuilder()
                .setBookingCode(bookingCode)
                .setFlightCode(flight.getFlightCode())
                .setAirlineName(airline.getName())
                .build());

        if (!checkIns.containsKey(booking)) {
            throw new IllegalStateException("Booking code does not have a check-in status.");
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
        if (requestedRangeCounters == null || requestedRangeCounters.isEmpty()) {
            throw new IllegalStateException("No counters assigned to the airline.");
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

