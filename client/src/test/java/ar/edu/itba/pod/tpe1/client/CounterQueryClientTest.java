package ar.edu.itba.pod.tpe1.client;

import airport.CounterServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Before;
import org.junit.Test;
/*
 * DISCLAIMER: These are not UNIT TESTS, they are INTEGRATION TESTS
 **/
public class CounterQueryClientTest {

    ManagedChannel channel;

    AirportAdminClient airportAdminClient;
    CounterQueryClient counterQueryClient;
    @Before
    public void setUp() throws Exception {
        channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();
        counterQueryClient = new CounterQueryClient(channel);
        airportAdminClient = new AirportAdminClient(channel);
    }
    @Test
    public void name() {
        airportAdminClient.addSector("A");
        airportAdminClient.addCounters("A", 5);

        airportAdminClient.addSector("B");
        airportAdminClient.addCounters("B", 8);

        counterQueryClient.queryCounters("A");
        counterQueryClient.queryCounters("");
    }
}