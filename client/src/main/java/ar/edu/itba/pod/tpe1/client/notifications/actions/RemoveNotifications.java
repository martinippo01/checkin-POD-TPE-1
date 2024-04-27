package ar.edu.itba.pod.tpe1.client.notifications.actions;

import java.util.List;

import airport.NotificationsServiceGrpc;
import airport.NotificationsServiceOuterClass;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.notifications.NotificationsAction;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import static ar.edu.itba.pod.tpe1.client.Arguments.AIRLINE;

public class RemoveNotifications extends NotificationsAction {
    private NotificationsServiceGrpc.NotificationsServiceBlockingStub blockingStub;

    public RemoveNotifications(List<String> actionArguments) {
        super(actionArguments);
    }

    private NotificationsServiceOuterClass.RemoveNotificationsRequest createRequest() {
        return NotificationsServiceOuterClass.RemoveNotificationsRequest.newBuilder()
                .setAirline(getArguments().get(AIRLINE.getArgument()))
                .build();
    }

    private NotificationsServiceOuterClass.RemoveNotificationsResponse notificationsResponse(
            NotificationsServiceOuterClass.RemoveNotificationsRequest request) {
        return blockingStub.removeNotifications(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = NotificationsServiceGrpc.newBlockingStub(channel);

        try {
            NotificationsServiceOuterClass.RemoveNotificationsRequest request = createRequest();
            NotificationsServiceOuterClass.RemoveNotificationsResponse response = notificationsResponse(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServerUnavailableException();
            }
        }
    }
}
