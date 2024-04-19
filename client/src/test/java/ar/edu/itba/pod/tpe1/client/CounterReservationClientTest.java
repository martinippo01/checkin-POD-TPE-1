package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
    public void queryCounterRange() {
    }

    @Test
    public void assignCounters() {
    }

    @Test
    public void freeCounters() {
    }

    @Test
    public void checkInCounters() {
    }

    @Test
    public void listPendingAssignments() {
    }
}