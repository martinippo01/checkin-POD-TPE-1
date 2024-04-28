package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.BasicResponse;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.CheckInCounterRequest;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.CounterReservationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class CheckInCounters extends CounterReservationAction {
    private CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public CheckInCounters(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CounterReservationServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());
        int fromVal = Integer.parseInt(getArguments().get(COUNTER_FROM.getArgument()));
        String airlineName = getArguments().get(AIRLINE.getArgument());

        CheckInCounterRequest request = CheckInCounterRequest.newBuilder()
                .setSectorName(sectorName)
                .setFromVal(fromVal)
                .setAirlineName(airlineName)
                .build();
        try {
            BasicResponse response = blockingStub.checkInCounters(request);
            System.out.println(response.getMessage());
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }
}
