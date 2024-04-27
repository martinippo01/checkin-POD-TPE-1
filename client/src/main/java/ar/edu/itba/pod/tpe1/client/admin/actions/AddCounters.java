package ar.edu.itba.pod.tpe1.client.admin.actions;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import ar.edu.itba.pod.tpe1.client.admin.AirportAdminAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.COUNTERS;
import static ar.edu.itba.pod.tpe1.client.Arguments.SECTOR;

public class AddCounters extends AirportAdminAction {
    private AirportAdminServiceGrpc.AirportAdminServiceBlockingStub blockingStub;

    public AddCounters(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = AirportAdminServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());
        int counterCount = Integer.parseInt(getArguments().get(COUNTERS.getArgument()));

        try {
            AirportService.CounterRequest request = AirportService.CounterRequest.newBuilder()
                    .setSectorName(sectorName)
                    .setCounterCount(counterCount)
                    .build();
            AirportService.CounterResponse response = blockingStub.addCounters(request);

            System.out.println(counterCount + " new counters (" + response.getFirstCounterId() + "-" + response.getLastCounterId() + ") in Sector " + response.getSectorName() + " added successfully");
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.err.println("Failed to add counters to sector: ");
            } else {
                System.err.println("Failed to add counters to sector: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
