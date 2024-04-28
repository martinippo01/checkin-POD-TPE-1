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

public final class ListCounters extends CounterReservationAction {
    private CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public ListCounters(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CounterReservationServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());
        int fromVal = Integer.parseInt(getArguments().get(COUNTER_FROM.getArgument()));
        int toVal = Integer.parseInt(getArguments().get(COUNTER_TO.getArgument()));

        CounterReservationServiceOuterClass.CounterRangeRequest request = CounterReservationServiceOuterClass.CounterRangeRequest.newBuilder()
                .setSectorName(sectorName)
                .setFromVal(fromVal)
                .setToVal(toVal)
                .build();
        try {
            CounterReservationServiceOuterClass.CounterRangeResponse response = blockingStub.queryCounterRange(request);
            if (response.getCountersList().isEmpty()) {
                System.out.println("No counters found in the specified range.");
                return;
            }
            System.out.println("Counters  Airline          Flights             People");
            System.out.println("##########################################################");
            response.getCountersList().forEach(counter -> {
                String flights = String.join("|", counter.getFlightsList());
                String line = String.format("(%d-%d)     %s %s   %d",
                        counter.getStart(), counter.getEnd(), counter.getAirline(),
                        flights.isEmpty() ? "-" : flights, counter.getPeopleWaiting());
                System.out.println(line);
            });
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.err.println("Error: The specified sector '" + sectorName + "' does not exist.");
            } else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                System.err.println("Error: The counter range must be positive");
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            } else {
                System.err.println("Failed: " + e.getStatus().asRuntimeException());
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
        }
    }
}
