package ar.edu.itba.pod.tpe1.client.query.actions;

import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.query.CounterQueryAction;
import io.grpc.ManagedChannel;

import java.util.List;

public final class CheckIns extends CounterQueryAction {
    public CheckIns(List<String> actionArguments, List<String> optionalActionArguments) {
        super(actionArguments, optionalActionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        throw new RuntimeException("Not implemented");
    }
}
