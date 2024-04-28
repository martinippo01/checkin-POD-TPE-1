package ar.edu.itba.pod.tpe1.client.notifications.actions;

import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.notifications.NotificationsAction;
import ar.edu.itba.pod.tpe1.protos.NotificationsService.NotificationsServiceGrpc;
import ar.edu.itba.pod.tpe1.protos.NotificationsService.RegisterNotificationsRequest;
import ar.edu.itba.pod.tpe1.protos.NotificationsService.RegisterNotificationsResponse;
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

    private RegisterNotificationsRequest createRequest() {
        return RegisterNotificationsRequest.newBuilder()
                .setAirline(getArguments().get(AIRLINE.getArgument()))
                .build();
    }

    private RegisterNotificationsResponse notificationsResponse(
            RegisterNotificationsRequest request) {
        try {
            Iterator<RegisterNotificationsResponse> response = blockingStub.registerNotifications(request);
            while (response.hasNext()) {
                RegisterNotificationsResponse registerNotificationsResponse;

                registerNotificationsResponse = response.next();

                // TODO: Will we print from here? Or modularize somewhere else?
                System.out.println(registerNotificationsResponse.getNotificationType());
                System.out.println(registerNotificationsResponse);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        return null; // TODO: Return something?
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = NotificationsServiceGrpc.newBlockingStub(channel);

        try {
            RegisterNotificationsRequest request = createRequest();
            RegisterNotificationsResponse response = notificationsResponse(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServerUnavailableException();
            }
        }
    }
}
