package ar.edu.itba.pod.tpe1.data;

import ar.edu.itba.pod.tpe1.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AirportTest {

    Airport airport;

    String sectorName = "T";
    String bookingCode = "ABC1234";
    String flightCode = "132ASF";
    String airlineName = "TestFlight";
    int counterRange = 3;
    int totalCounters = 10;

    @Before
    public void setUp() throws Exception {
        airport = Airport.getInstance();

        List<String> flights = new ArrayList<>(1);
        flights.add(flightCode);

        airport.addSector(sectorName);
        airport.addCounters(sectorName, totalCounters);
        airport.registerPassenger(bookingCode, flightCode, airlineName);
        airport.assignCounters(sectorName, counterRange, airlineName, flights);
    }

    @After
    public void tearDown() throws Exception {
        // Destroy Airport instance between tests
        Field field = airport.getClass().getDeclaredField("instance");
        field.setAccessible(true);
        field.set(airport, null);
    }

    @Test
    public void test_performCheckIn_singlePassenger() throws Exception {
        // Add to queue
        PassengerCheckinResponse passengerCheckinResponse = airport.addToCheckInQueue(bookingCode, sectorName, 1).build();
        assertEquals(CheckinStatus.CHECKIN_STATUS_ADDED_TO_QUEUE, passengerCheckinResponse.getStatus());

        // Perform check-in
        CheckInCountersResponse checkInDone = airport.performCheckIn(sectorName, 1, airlineName).build();

        assertEquals(CheckInCountersStatus.CHECK_IN_COUNTERS_STATUS_CHECKIN_DONE, checkInDone.getStatus());
        assertEquals(3, checkInDone.getDataCount());

        CheckInCounterInformation information = checkInDone.getData(0);
        assertEquals(CheckInCounterStatus.CHECK_IN_COUNTER_STATUS_SUCCESS, information.getStatus());
        assertTrue(information.hasBooking());
        assertEquals(bookingCode, information.getBooking().getBookingCode());
        assertEquals(flightCode, information.getBooking().getFlightCode());
        assertEquals(airlineName, information.getBooking().getAirlineName());
        assertEquals(1, information.getCounter());


        for (int i = 1; i < counterRange; i++) {
            information = checkInDone.getData(i);

            assertEquals(CheckInCounterStatus.CHECK_IN_COUNTER_STATUS_IDLE, information.getStatus());
            assertFalse(information.hasBooking());
            assertEquals(i + 1, information.getCounter());
        }
    }

    @Test
    public void test_listAssignedCounters_valid_id() throws Exception {
        FetchCounterResponse fetchCounterResponse = airport.listAssignedCounters(bookingCode).build();
        assertEquals(CounterStatus.COUNTER_STATUS_COUNTERS_ASSIGNED, fetchCounterResponse.getStatus());
        assertEquals(bookingCode, fetchCounterResponse.getBooking().getBookingCode());
        assertEquals(flightCode, fetchCounterResponse.getBooking().getFlightCode());
        assertEquals(airlineName, fetchCounterResponse.getBooking().getAirlineName());
        assertEquals(1, fetchCounterResponse.getDataList().size());

        CountersInformation information = fetchCounterResponse.getDataList().get(0);
        assertEquals(1, information.getCounters().getFirstCounterNumber());
        assertEquals(counterRange, information.getCounters().getNumberOfConsecutiveCounters());
        assertEquals(sectorName, information.getSectorName());
        assertEquals(0, information.getPeopleInQueue());
    }

    @Test
    public void test_addToCheckInQueue_out_of_queue() throws Exception {
        PassengerCheckinResponse passengerCheckinResponse = airport.addToCheckInQueue(bookingCode, sectorName, 1).build();
        assertEquals(CheckinStatus.CHECKIN_STATUS_ADDED_TO_QUEUE, passengerCheckinResponse.getStatus());
        assertEquals(bookingCode, passengerCheckinResponse.getBooking().getBookingCode());
        assertEquals(flightCode, passengerCheckinResponse.getBooking().getFlightCode());
        assertEquals(airlineName, passengerCheckinResponse.getBooking().getAirlineName());
        assertTrue(passengerCheckinResponse.hasData());

        CountersInformation information = passengerCheckinResponse.getData();
        assertEquals(1, information.getCounters().getFirstCounterNumber());
        assertEquals(counterRange, information.getCounters().getNumberOfConsecutiveCounters());
        assertEquals(sectorName, information.getSectorName());
        assertEquals(1, information.getPeopleInQueue());
    }

    @Test
    public void test_addToCheckInQueue_already_in_queue() throws Exception {
        // Add to queue
        PassengerCheckinResponse passengerCheckinResponse = airport.addToCheckInQueue(bookingCode, sectorName, 1).build();
        assertEquals(CheckinStatus.CHECKIN_STATUS_ADDED_TO_QUEUE, passengerCheckinResponse.getStatus());

        // Try to add again
        passengerCheckinResponse = airport.addToCheckInQueue(bookingCode, sectorName, 1).build();

        assertEquals(CheckinStatus.CHECKIN_STATUS_PASSENGER_ALREADY_IN_QUEUE, passengerCheckinResponse.getStatus());
        assertEquals(bookingCode, passengerCheckinResponse.getBooking().getBookingCode());
        assertEquals(flightCode, passengerCheckinResponse.getBooking().getFlightCode());
        assertEquals(airlineName, passengerCheckinResponse.getBooking().getAirlineName());
        assertTrue(passengerCheckinResponse.hasData());

        CountersInformation information = passengerCheckinResponse.getData();
        assertEquals(1, information.getCounters().getFirstCounterNumber());
        assertEquals(counterRange, information.getCounters().getNumberOfConsecutiveCounters());
        assertEquals(sectorName, information.getSectorName());
        assertEquals(1, information.getPeopleInQueue());
    }

    @Test
    public void test_addToCheckInQueue_already_done() throws Exception {
        // Add to queue
        PassengerCheckinResponse passengerCheckinResponse = airport.addToCheckInQueue(bookingCode, sectorName, 1).build();
        assertEquals(CheckinStatus.CHECKIN_STATUS_ADDED_TO_QUEUE, passengerCheckinResponse.getStatus());

        // Perform check-in
        CheckInCountersResponse checkInDone = airport.performCheckIn(sectorName, 1, airlineName).build();
        assertEquals(CheckInCountersStatus.CHECK_IN_COUNTERS_STATUS_CHECKIN_DONE, checkInDone.getStatus());

        // Try to add again
        passengerCheckinResponse = airport.addToCheckInQueue(bookingCode, sectorName, 1).build();

        assertEquals(CheckinStatus.CHECKIN_STATUS_CHECKIN_ALREADY_DONE, passengerCheckinResponse.getStatus());
        assertEquals(bookingCode, passengerCheckinResponse.getBooking().getBookingCode());
        assertEquals(flightCode, passengerCheckinResponse.getBooking().getFlightCode());
        assertEquals(airlineName, passengerCheckinResponse.getBooking().getAirlineName());
        assertTrue(passengerCheckinResponse.hasData());

        CountersInformation information = passengerCheckinResponse.getData();
        assertEquals(1, information.getCounters().getFirstCounterNumber());
        assertEquals(counterRange, information.getCounters().getNumberOfConsecutiveCounters());
        assertEquals(sectorName, information.getSectorName());
        assertEquals(0, information.getPeopleInQueue());
    }
}