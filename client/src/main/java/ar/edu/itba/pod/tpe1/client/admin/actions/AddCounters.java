package ar.edu.itba.pod.tpe1.client.admin.actions;

import ar.edu.itba.pod.tpe1.client.admin.AirportAdminAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.AirportService.AirportAdminServiceGrpc;
import ar.edu.itba.pod.tpe1.protos.AirportService.CounterRequest;
import ar.edu.itba.pod.tpe1.protos.AirportService.CounterResponse;
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
            CounterRequest request = CounterRequest.newBuilder()
                    .setSectorName(sectorName)
                    .setCounterCount(counterCount)
                    .build();
            CounterResponse response = blockingStub.addCounters(request);

            System.out.println(counterCount + " new counters (" + response.getFirstCounterId() + "-" + response.getLastCounterId() + ") in Sector " + response.getSectorName() + " added successfully");
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.err.println("Failed to add counters to sector: ");
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            } else {
                System.err.println("Failed to add counters to sector: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
