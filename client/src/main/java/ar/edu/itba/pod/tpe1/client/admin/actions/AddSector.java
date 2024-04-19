package ar.edu.itba.pod.tpe1.client.admin.actions;

import ar.edu.itba.pod.tpe1.client.admin.AirportAdminAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;

import java.util.List;

public class AddSector extends AirportAdminAction {
    public AddSector(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        // TODO: Implement
    }
}
