package ar.edu.itba.pod.tpe1.client.notifications.actions;

import airport.NotificationsServiceGrpc;
import airport.NotificationsServiceOuterClass;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.notifications.NotificationsAction;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.Iterator;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.AIRLINE;

public final class RegisterNotifications extends NotificationsAction {
    private NotificationsServiceGrpc.NotificationsServiceBlockingStub blockingStub;

    public RegisterNotifications(List<String> actionArguments) {
        super(actionArguments);
    }

    private NotificationsServiceOuterClass.RegisterNotificationsRequest createRequest() {
        return NotificationsServiceOuterClass.RegisterNotificationsRequest.newBuilder()
                .setAirline(getArguments().get(AIRLINE.getArgument()))
                .build();
    }

    private NotificationsServiceOuterClass.RegisterNotificationsResponse notificationsResponse(
            NotificationsServiceOuterClass.RegisterNotificationsRequest request) {
        Iterator<NotificationsServiceOuterClass.RegisterNotificationsResponse> response = blockingStub.registerNotifications(request);

        while (response.hasNext()) {
            NotificationsServiceOuterClass.RegisterNotificationsResponse registerNotificationsResponse = response.next();
            // TODO: Will we print from here? Or modularize somewhere else?
            System.out.println(registerNotificationsResponse.getNotificationType());
            System.out.println(registerNotificationsResponse);
        }

        return null; // TODO: Return something?
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = NotificationsServiceGrpc.newBlockingStub(channel);

        try {
            NotificationsServiceOuterClass.RegisterNotificationsRequest request = createRequest();
            NotificationsServiceOuterClass.RegisterNotificationsResponse response = notificationsResponse(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServerUnavailableException();
            }
        }
    }
}
