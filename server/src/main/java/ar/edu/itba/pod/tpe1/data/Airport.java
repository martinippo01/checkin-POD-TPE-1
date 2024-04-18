package ar.edu.itba.pod.tpe1.data;

import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.utils.*;
import checkin.CheckinServiceOuterClass;
import counter.CounterReservationServiceOuterClass;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Airport {

    private final Set<Booking> bookingCodes = Collections.synchronizedSet(new HashSet<>());
    private final Set<Sector> sectors = Collections.synchronizedSet(new HashSet<>());
    private final Set<Airline> airlines = Collections.synchronizedSet(new HashSet<>());

    private final AtomicInteger counterId = new AtomicInteger(1);

    private static Airport instance = null;

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
        if (sectorName == null || sectorName.isEmpty()) {
            return false; // Failure, sector already exists
        }

        sectors.add(new Sector(sectorName));
        return true;
    }

    // Adds a set of counters to a sector
    public Integer addCounters(String sectorName, int count) {
        Sector sector = new Sector(sectorName);
        if (sectorName == null || sectorName.isEmpty() || sectors.contains(sector) || count <= 0) {
            return null;
        }

        int firstId = counterId.getAndAdd(count);
        sector.addCounter(new RangeCounter(firstId, firstId + count));
        sectors.add(sector);

        return firstId; // Success, returns the first ID of the new counters
    }

    // Register a passenger, link booking and flight codes
    public boolean registerPassenger(String bookingCode, String flightCode, String airlineName) {
        Booking booking = new Booking(bookingCode);
        Flight flight = new Flight(flightCode);
        Airline airline = new Airline(airlineName);
        airline.setFlight(flight);
        booking.setAirline(airline);

        if (bookingCodes.contains(booking)) {
            return false;
        }

        for (Airline air : airlines) {
            // Same flight code, different airline
            if (air.getFlight().equals(flight) && !air.getName().equals(airlineName)) {
                return false;
            }
        }

        airlines.add(airline);
        bookingCodes.add(booking);
        return true; // Success
    }

    public List<CounterServiceOuterClass.CounterInfo> queryCounters(String sector) {
        if (sector == null)
            return new ArrayList<>();

        //it should return all counters if sector is null
//        if(Objects.equals(sector, ""))
//            return counterDetails.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
//
//        return new ArrayList<>(counterDetails.get(sector).values());

        // TODO: Finish this after agreeing on proper data modeling
        return sectors.stream().map(sec -> CounterServiceOuterClass.CounterInfo.newBuilder()
                .setSector(sec.getName())
                .setRange(sec.getAssignedCounters().stream().findFirst().orElse(new RangeCounter(-5, -1)).toString())
                .build()
        ).toList();
    }

    public CheckinServiceOuterClass.BookingInformation getBookingInformation(String bookingCode) {
        if (bookingCodes.contains(new Booking(bookingCode))) {
            return null;
        }

        Booking booking = null;
        for (Booking possibleBooking : bookingCodes) {
            if (possibleBooking.getCode().equals(bookingCode)) {
                booking = possibleBooking;
            }
        }

        // Booking has a valid Flight
        if (booking == null || booking.getAirline() == null || booking.getAirline().getFlight() == null || booking.getAirline().getFlight().flightCode().isEmpty()) {
            return null;
        }

        return CheckinServiceOuterClass.BookingInformation.newBuilder()
                .setBookingCode(bookingCode)
                .setFlightCode(booking.getAirline().getFlight().flightCode())
                .setAirlineName(booking.getAirline().getName())
                .build();
    }

    public CheckinServiceOuterClass.CountersInformation countersByBooking(CheckinServiceOuterClass.BookingInformation bookingInformation) {

        int lowestRange = 0;
        int highestRange = 0;
        int totalPeopleInQueue = 0;

        Airline airline = null;
        for (Booking booking : bookingCodes) {
            if (booking.getCode().equals(bookingInformation.getBookingCode())) {
                airline = booking.getAirline();
                break;
            }
        }

        // TODO: Finish after deciding proper data modeling
        return null;
//
//        for (ConcurrentHashMap<Integer, CounterServiceOuterClass.CounterInfo> counterMap : counterDetails.values()) {
//            for (CounterServiceOuterClass.CounterInfo counterInfo : counterMap.values()) {
//                if (!counterInfo.getAirline().equals(bookingInformation.getAirlineName())) {
//                    continue;
//                }
//
//                lowestRange = Math.min(lowestRange, parseInt(counterInfo.getRange()));
//                highestRange = Math.max(highestRange, parseInt(counterInfo.getRange()));
//                totalPeopleInQueue += counterInfo.getWaitingPeople();
//                if (sector.isEmpty()) {
//                    sector = counterInfo.getSector();
//                }
//            }
//        }
//
//        CheckinServiceOuterClass.CounterRange counters = CheckinServiceOuterClass.CounterRange.newBuilder()
//                .setFirstCounterNumber(lowestRange)
//                .setNumberOfConsecutiveCounters(highestRange - lowestRange)
//                .build();
//
//        return CheckinServiceOuterClass.CountersInformation.newBuilder()
//                .setCounters(counters)
//                .setSectorName(sector)
//                .setPeopleInQueue(totalPeopleInQueue)
//                .build();
    }
}

