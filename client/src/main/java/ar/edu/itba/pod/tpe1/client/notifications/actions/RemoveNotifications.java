package ar.edu.itba.pod.tpe1.client.notifications.actions;

import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.notifications.NotificationsAction;
import ar.edu.itba.pod.tpe1.protos.NotificationsService.NotificationsServiceGrpc;
import ar.edu.itba.pod.tpe1.protos.NotificationsService.RemoveNotificationsRequest;
import ar.edu.itba.pod.tpe1.protos.NotificationsService.RemoveNotificationsResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.AIRLINE;

public class RemoveNotifications extends NotificationsAction {
    private NotificationsServiceGrpc.NotificationsServiceBlockingStub blockingStub;

    public RemoveNotifications(List<String> actionArguments) {
        super(actionArguments);
    }

    private RemoveNotificationsRequest createRequest() {
        return RemoveNotificationsRequest.newBuilder()
                .setAirline(getArguments().get(AIRLINE.getArgument()))
                .build();
    }

    private RemoveNotificationsResponse notificationsResponse(RemoveNotificationsRequest request) {
        return blockingStub.removeNotifications(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = NotificationsServiceGrpc.newBlockingStub(channel);

        try {
            RemoveNotificationsRequest request = createRequest();
            RemoveNotificationsResponse response = notificationsResponse(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            } else if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                System.err.println(e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
