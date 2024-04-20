package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/*
* DISCLAIMER: These are not UNIT TESTS, they are INTEGRATION TESTS
**/
public class AirportAdminClientTest {

   ManagedChannel channel;

    AirportAdminClient airportAdminClient;
    @Before
    public void setUp() throws Exception {
         channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();
        airportAdminClient = new AirportAdminClient(channel);
    }

    @Test
    public void testAddSector(){
        airportAdminClient.addSector("A");
        // Should fail to add second sector
        airportAdminClient.addSector("A");
    }

    @Test
    public void testAddCounterAndSector() {
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

        // Should fail: Add 3 counters to F (does not exist)
        airportAdminClient.addCounters("F", 3);
        // Should fail: Add a negative amount of counters
        airportAdminClient.addCounters("A", -3);
    }

    @Test
    public void testAddCounterAndSectorWhenContiguos() {
        // Create sector A and add 1 counter
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 1);
        airportAdminClient.addCounters("A", 2);
    }

    @Test
    public void testAddPassengerManifest() {
        // This csv is correct
        System.out.println("Case 1: All OK");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersOk.csv");
        // Should fail, repeated booking code
        System.out.println("Case 2: Repeated booking");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersErrorCase1.csv");
        // Should fail, same flight different airline
        System.out.println("Case 3: Same flight different airline");
        airportAdminClient.addPassengerManifest("/home/martinippo/Desktop/ITBA/POD/TPE1/grpc-com-tpe1/client/src/test/java/ar/edu/itba/pod/tpe1/client/passengersErrorCase2.csv");

    }

    @After
    public void tearDown() throws Exception {
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }
}