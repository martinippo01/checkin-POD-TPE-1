package ar.edu.itba.pod.tpe1.client.query.actions;

import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.query.CounterQueryAction;
import io.grpc.ManagedChannel;

import java.util.List;

public final class QueryCounters extends CounterQueryAction {
    public QueryCounters(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        throw new RuntimeException("Not implemented");
    }
}
