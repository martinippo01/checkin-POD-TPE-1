package ar.edu.itba.pod.tpe1.client;

import airport.CounterServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/*
 * DISCLAIMER: These are not UNIT TESTS, they are INTEGRATION TESTS. They require the server to be running
 * and ideally re-started before running each test.
 **/
public class CounterQueryClientTest {

    ManagedChannel channel;

    AirportAdminClient airportAdminClient;
    CounterQueryClient counterQueryClient;

    CounterReservationClient counterReservationClient;
    @Before
    public void setUp() throws Exception {
        channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();
        counterQueryClient = new CounterQueryClient(channel);
        airportAdminClient = new AirportAdminClient(channel);
        counterReservationClient = new CounterReservationClient(channel);
    }
    @Test
    public void queryCounters() {
        counterQueryClient.queryCounters("C");

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
        airportAdminClient.addPassengerManifest("src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");

        List<String> flights = new ArrayList<>();
        flights.add("AC987");
        flights.add("AC988");
        counterReservationClient.assignCounters("C", flights, "AirCanada", 2);
        counterReservationClient.assignCounters("D", flights, "AirCanada", 2);
        counterReservationClient.listSectors();

        counterQueryClient.queryCounters("C");
        counterQueryClient.queryCounters("Z");
        counterQueryClient.queryCounters("");

    }


}