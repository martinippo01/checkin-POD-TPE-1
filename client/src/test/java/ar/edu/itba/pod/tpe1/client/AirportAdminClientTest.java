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
    public void testAddCounterAndSector() {
        airportAdminClient.addCounters("A", 5);
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 5);
    }

    @Test
    public void testAddPassengerManifest() {
        airportAdminClient.addPassengerManifest("/Users/marcoscilipoti/Documents/1Q 2024/POD/checkin-POD-TPE-1/client/src/main/resources/manifest.csv");
    }

    @After
    public void tearDown() throws Exception {
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }
}