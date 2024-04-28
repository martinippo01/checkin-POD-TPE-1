package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.AssignCounterRequest;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.AssignCounterResponse;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.CounterReservationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.Arrays;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class AssignCounters extends CounterReservationAction {
    private CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public AssignCounters(List<String> actionArguments) {
        super(actionArguments);
    }

    List<String> parseFlightsArgument(String flights) {
        return Arrays.stream(flights.strip().split("\\|")).toList();
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CounterReservationServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());
        List<String> flights = parseFlightsArgument(getArguments().get(FLIGHTS.getArgument()));
        String airlineName = getArguments().get(AIRLINE.getArgument());
        int counterCount = Integer.parseInt(getArguments().get(COUNTER_COUNT.getArgument()));

        AssignCounterRequest request = AssignCounterRequest.newBuilder()
                .setSectorName(sectorName)
                .addAllFlights(flights)
                .setAirlineName(airlineName)
                .setCounterCount(counterCount)
                .build();
        try {
            AssignCounterResponse response = blockingStub.assignCounters(request);
            if (!(response.getIsPending())) {
                String airlines = String.join("|", flights);
                System.out.println(counterCount + " counters (" + response.getCounterFrom() + "-" + String.valueOf(response.getCounterFrom() + counterCount - 1) + ") in Sector " + sectorName + " are now checking in passengers from " +
                        airlineName + " " + airlines + " flights\n");
            } else {
                System.out.println(counterCount + " counters in Sector " + sectorName + " is pending with " + response.getPendingAhead() + " other pendings ahead\n");
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                System.err.println(e.getMessage());
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
