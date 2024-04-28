package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import counter.CounterReservationServiceGrpc;
import counter.CounterReservationServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class FreeCounters extends CounterReservationAction {
    private CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public FreeCounters(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CounterReservationServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());
        int fromVal = Integer.parseInt(getArguments().get(COUNTER_FROM.getArgument()));
        String airlineName = getArguments().get(AIRLINE.getArgument());

        CounterReservationServiceOuterClass.FreeCounterRequest request = CounterReservationServiceOuterClass.FreeCounterRequest.newBuilder()
                .setSectorName(sectorName)
                .setFromVal(fromVal)
                .setAirlineName(airlineName)
                .build();
        try {
            CounterReservationServiceOuterClass.FreeCounterResponse response = blockingStub.freeCounters(request);
            String range = " on counters " + response.getRangeStart() + "-" + response.getRangeEnd();
            System.out.println("Ended check-in for flights " + String.join("|", response.getFlightNumbersList()) + range + " in Sector " + response.getSectorName());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.err.println("Error: The specified sector '" + sectorName + "' does not exist.");
            } else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                System.err.println("Error: The specified counter range starting at " + fromVal + " does not exist in sector '" + sectorName + "'.");
            } else if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                System.err.println("Error: The counter range cannot be freed as it is not assigned to '" + airlineName + "'.");
            } else if (e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
                System.err.println("Error: Cannot free counters as there are passengers waiting to be attended.");
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
        }
    }
}
