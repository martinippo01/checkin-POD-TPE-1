package ar.edu.itba.pod.tpe1.client.query.actions;

import airport.CounterServiceGrpc;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.query.CounterQueryAction;
import io.grpc.ManagedChannel;

import java.util.List;

public final class Counters extends CounterQueryAction {
    public Counters(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        throw new RuntimeException("Not implemented");
    }
}
