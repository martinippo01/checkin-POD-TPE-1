package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AirportAdminClientTest {

   ManagedChannel channel;

    @Before
    public void setUp() throws Exception {
         channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();
    }

    @Test
    public void name() {
        AirportAdminClient airportAdminClient = new AirportAdminClient(channel);
        airportAdminClient.addCounters("A", 5);
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 5);
    }

    @After
    public void tearDown() throws Exception {
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }
}