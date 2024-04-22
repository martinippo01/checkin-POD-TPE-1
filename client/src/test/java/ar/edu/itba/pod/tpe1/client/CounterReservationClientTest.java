package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/*
*   WARNING: This are not unit tests!
*   The following test are made to check from client to server. As if they were "Integration tests"
* */

public class CounterReservationClientTest {

    private static Logger logger = LoggerFactory.getLogger(Client.class);

    ManagedChannel channel;
    CounterReservationClient counterReservationClient;
    AirportAdminClient airportAdminClient;

    @Before
    public void setUp() throws Exception {
        channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();
        counterReservationClient = new CounterReservationClient(channel);
        airportAdminClient = new AirportAdminClient(channel);
    }

    @After
    public void tearDown() throws Exception {
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void listSectors() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);
        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        counterReservationClient.listSectors();
    }

    @Test
    public void listSectorsFailNoSectors() {
        counterReservationClient.listSectors();
    }

    @Test
    public void queryCounterRange() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        flights.add("AC988");
        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);
        counterReservationClient.listSectors();

        counterReservationClient.queryCounterRange("C", 2, 4);
        counterReservationClient.queryCounterRange("C", 1, 2);
        counterReservationClient.queryCounterRange("C", 7, 9);
        counterReservationClient.queryCounterRange("C", 3, 9);
    }

    @Test
    public void queryCounterRangeFailNoSectors() {

        counterReservationClient.queryCounterRange("C", 2, 4);

        airportAdminClient.addSector("A");

        counterReservationClient.queryCounterRange("C", 2, 4);
    }

    @Test
    public void queryCounterRangeFailInvalidRange() {

        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 3);

        counterReservationClient.queryCounterRange("C", 4, 2);
    }

    @Test
    public void assignCounters() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);
        counterReservationClient.listSectors();
    }

    @Test
    public void assignCountersFailNoSectors() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("X", flights, "AirCanada", 2);
        counterReservationClient.listSectors();
    }

    @Test
    public void assignCountersFailFlightNotRegistered() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("FO542");
        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);
        counterReservationClient.listSectors();
    }

    @Test
    public void assignCountersFailFlightFromOtherAirline() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("C", flights, "AmericanAirline", 2);
        counterReservationClient.listSectors();
    }

    @Test
    public void assignCountersFailFlightInOtherCounter() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);

        List<String> flights2 = new ArrayList<>();
        flights2.add("AC988");
        flights2.add("AC987");
        counterReservationClient.assignCounters("D", flights2, "AirCanada", 2);

        counterReservationClient.listSectors();
    }

    @Test
    public void assignCountersFailFlightIsAlreadyPending() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("A", flights, "AirCanada", 2); // Will be pending

        counterReservationClient.assignCounters("D", flights, "AirCanada", 2);

        counterReservationClient.listSectors();
    }

    @Test
    public void assignCountersFailFlightAssignedInThePast() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);
        counterReservationClient.freeCounters("C", 2, "AirCanada");
        counterReservationClient.assignCounters("D", flights, "AirCanada", 2);

        counterReservationClient.listSectors();
    }

    @Test
    public void freeCounters() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        // Create sector C and add 3 counters
        airportAdminClient.addSector("C");
        airportAdminClient.addCounters("C", 3);
        // Create sector D and add 2 counters
        airportAdminClient.addSector("D");
        airportAdminClient.addCounters("D", 2);
        // Add 2 more counters to C
        airportAdminClient.addCounters("C", 2);

        airportAdminClient.addCounters("D", 2);
        airportAdminClient.addCounters("C", 2);

        // Create sector Z and leave it empty
        airportAdminClient.addSector("Z");

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);
        counterReservationClient.listSectors();

        counterReservationClient.queryCounterRange("C", 1, 3);
        counterReservationClient.freeCounters("C", 2,"AirCanada" );
        counterReservationClient.freeCounters("C", 2,"AirCanada" );
        counterReservationClient.freeCounters("F", 2,"AirCanada" );

        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);
        counterReservationClient.freeCounters("C", 2,"AirCanada2" );

        counterReservationClient.listSectors();

    }

    @Test
    public void checkInCounters() {

    }

    @Test
    public void assignPendingWhenFreeCounter(){
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 3);

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        // Assign the first Flight-Airline
        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("A", flights, "AirCanada", 2);
        counterReservationClient.listSectors();

        // Assign the second one and it'll be pending
        List<String> flights1 = new ArrayList<>();
        flights1.add("AC988");
        counterReservationClient.assignCounters("A", flights1, "AirCanada", 2);
        counterReservationClient.listPendingAssignments("A");

        // Free the counters for flight AC987
        counterReservationClient.freeCounters("A", 1,"AirCanada" );

        // Check if the assignation was made
        counterReservationClient.listPendingAssignments("A");

    }

    @Test
    public void assignPendingWhenAddedCountersToSector(){
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 3);

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        // Assign the first Flight-Airline
        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        counterReservationClient.assignCounters("A", flights, "AirCanada", 2);
        counterReservationClient.listSectors();

        // Assign the second one and it'll be pending
        List<String> flights1 = new ArrayList<>();
        flights1.add("AC988");
        counterReservationClient.assignCounters("A", flights1, "AirCanada", 2);
        counterReservationClient.listPendingAssignments("A");

        // Free the counters for flight AC987
        airportAdminClient.addCounters("A", 2);

        // Check if the assignation was made
        counterReservationClient.listPendingAssignments("A");

    }

    @Test
    public void listPendingAssignments() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 5);

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights1 = new ArrayList<>();
        flights1.add("AC987");
        counterReservationClient.assignCounters("A", flights1, "AirCanada", 2);


        List<String> flights3 = new ArrayList<>();
        flights3.add("AA123");
        counterReservationClient.assignCounters("A", flights3, "AmericanAirlines", 3);

        List<String> flights2 = new ArrayList<>();
        flights2.add("AC988");
        counterReservationClient.assignCounters("A", flights2, "AirCanada", 3);

        System.out.println("Prior additions of counter");
        counterReservationClient.listPendingAssignments("A");

        airportAdminClient.addCounters("A", 5);

        System.out.println("Post additions of counter");
        counterReservationClient.listPendingAssignments("A");

        counterReservationClient.listSectors();
        counterReservationClient.queryCounterRange("A", 1, 20);
    }

    @Test
    public void listPendingAssignmentsWhenNoPending() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 10);

        // Add passengers
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights1 = new ArrayList<>();
        flights1.add("AC987");
        counterReservationClient.assignCounters("A", flights1, "AirCanada", 2);

        List<String> flights2 = new ArrayList<>();
        flights2.add("AC988");
        counterReservationClient.assignCounters("A", flights2, "AirCanada", 3);

        List<String> flights3 = new ArrayList<>();
        flights3.add("AA123");
        counterReservationClient.assignCounters("A", flights3, "AmericanAirlines", 3);

        counterReservationClient.listPendingAssignments("A");
        counterReservationClient.listSectors();
    }
}