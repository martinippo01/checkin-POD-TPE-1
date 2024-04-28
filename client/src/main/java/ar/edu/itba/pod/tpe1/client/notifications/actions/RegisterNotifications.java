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

    private void handleNotification(RegisterNotificationsResponse response) {
        switch (response.getNotificationType()) {
            case SUCCESSFUL_REGISTER:
                System.out.printf("%s registered successfully for events\n", getArguments().get(AIRLINE.getArgument()));
                break;
            case COUNTERS_ASSIGNED:
                System.out.printf("%d counters (%d-%d) in Sector %s are now checking in passengers from %s flights\n",
                        response.getCounterTo() - response.getCounterFrom() + 1,
                        response.getCounterFrom(), response.getCounterTo(), response.getSector(), String.join("|", response.getFlightsList()));
                break;
            case NEW_BOOKING_IN_QUEUE:
                System.out.printf("Booking %s for flight %s from %s is now waiting to check-in on counters (%d-%d) in Sector %s with %d people in line\n",
                        response.getBooking(), response.getFlight(), response.getAirline(), response.getCounterFrom(), response.getCounterTo(), response.getSector(), response.getPeopleAhead());
                break;
            case CHECK_IN_SUCCESSFUL:
                System.out.printf("Check-in successful of %s for flight %s at counter %d in Sector %s\n",
                        response.getBooking(), response.getFlight(), response.getCounter(), response.getSector());
                break;
            case COUNTERS_REMOVED:
                System.out.printf("Ended check-in for flights %s on counters (%d-%d) from Sector %s\n",
                        String.join("|", response.getFlightsList()), response.getCounterFrom(), response.getCounterTo(), response.getSector());
                break;
            case COUNTERS_PENDING:
                System.out.printf("%d counters in Sector %s for flights %s is pending with %d other pendings ahead\n",
                        response.getCounter(), response.getSector(), String.join("|", response.getFlightsList()), response.getPendingAhead());
                break;
            case COUNTERS_UPDATE:
                System.out.printf("%d counters in Sector %s for flights %s were updated with %d other pendings ahead\n",
                        response.getCounter(), response.getSector(), String.join("|", response.getFlightsList()), response.getPendingAhead());
                break;
            default:
                System.out.println("Unhandled notification type.");
                break;
        }
    }

    private void notificationsResponse(RegisterNotificationsRequest request) {
        Iterator<RegisterNotificationsResponse> response = blockingStub.registerNotifications(request);
        while (response.hasNext()) {
            RegisterNotificationsResponse registerNotificationsResponse;

            registerNotificationsResponse = response.next();
            handleNotification(registerNotificationsResponse);
        }
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = NotificationsServiceGrpc.newBlockingStub(channel);

        try {
            RegisterNotificationsRequest request = createRequest();
            notificationsResponse(request);
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
