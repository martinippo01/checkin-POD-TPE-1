package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import counter.CounterReservationServiceGrpc;
import io.grpc.ManagedChannel;

import java.util.List;

public final class ListPendingAssignments extends CounterReservationAction {
    private CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public ListPendingAssignments(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        // TODO: Implement
        throw new RuntimeException("Not implemented");
    }
}
