package ar.edu.itba.pod.tpe1.servant;

import airport.NotificationsServiceGrpc;
import airport.NotificationsServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.Notifications;
import ar.edu.itba.pod.tpe1.data.utils.Airline;
import ar.edu.itba.pod.tpe1.data.utils.Notification;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.spec.ECField;

public class NotificationsServant extends NotificationsServiceGrpc.NotificationsServiceImplBase {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(NotificationsServant.class);

    // State
    private final Airport airport = Airport.getInstance();
    private final Notifications notifications = Notifications.getInstance();

    @Override
    public void registerNotifications(
            NotificationsServiceOuterClass.RegisterNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RegisterNotificationsResponse> responseObserver
    ){
        try {
            logger.info("Airline: {} requested register to notifications service", req.getAirline());
            Airline airline = new Airline(req.getAirline());

            // Check that the airline exists at the airport
            if(!airport.airlineExists(airline)){
                logger.error("Airline: {} failed to register to notifications service, it is not registered at the airport", airline.getName());
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Airline does not exists at the airport").asRuntimeException());
                return;
            }

            // First register the airline
            try {
                notifications.registerAirline(airline);
            }catch (Exception e){
                logger.error("Airline: {} failed to register to notifications service, it is already registered", airline.getName());
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
                return;
            }

            logger.info("Airline: {} successfully registered to notifications service", airline.getName());
            responseObserver.onNext(buildNotificationProto(new Notification.Builder().setNotificationType(NotificationsServiceOuterClass.NotificationType.SUCCESSFUL_REGISTER).build()));

            // Stream the notifications
            Notification notification;
            do {
                notification = notifications.getNotification(airline);
                if (notification != null && !notification.isPoissonPill()) {
                    responseObserver.onNext(buildNotificationProto(notification));
                    logger.info("Sent notification of type {} to airline: {}", notification.getNotificationType(), airline.getName());
                }
            } while (notification != null && !notification.isPoissonPill()); // null is the poison pill - For when no more notifications need to be sent to the client

            // Once consumed the poisson pill, remove the airline from the airport
            notifications.removeAirline(airline);

            // Complete
            responseObserver.onCompleted();

        }catch (InterruptedException e){
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }

    }

    @Override
    public void removeNotifications(
            NotificationsServiceOuterClass.RemoveNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RemoveNotificationsResponse> responseObserver
    ){
        Airline airline = new Airline(req.getAirline());

        logger.info("Airline: {} requested to be removed from notifications service", airline.getName());

        // Unregister the airline
        try {
            notifications.unregisterAirline(airline);
        }catch (Exception e){
            logger.error("Airline: {} failed to unregister to notifications service", airline.getName());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            return;
        }

        // In case the airline was registered, and now is not. Response goes empty
        responseObserver.onNext(NotificationsServiceOuterClass.RemoveNotificationsResponse.newBuilder().build());
        responseObserver.onCompleted();
        logger.info("Airline: {} successfully unregistered to notifications service", airline.getName());
    }

    private NotificationsServiceOuterClass.RegisterNotificationsResponse buildNotificationProto(Notification notification){
        if(notification == null) return null;
        // TODO: Check notification type, in order to set the respective arguments of the proto messsage
        NotificationsServiceOuterClass.RegisterNotificationsResponse.Builder builder = NotificationsServiceOuterClass.RegisterNotificationsResponse.newBuilder();

        System.out.println("Airline name " + notification.getAirline());
        System.out.println("Flights " + notification.getFlights());

        builder.setNotificationType(notification.getNotificationType())
                .setAirline(notification.getAirline().getName())
                .setCounterFrom(notification.getCounterFrom())
                .setCounterTo(notification.getCounterTo())
                .setSector(notification.getSector())
                .addAllFlights(notification.getFlights())
                .setBooking(notification.getBooking())
                .setFlight(notification.getFlight())
                .setPeopleAhead(notification.getPeopleAhead())
                .setCounter(notification.getCounter())
                .setPendingAhead(notification.getPendingAhead());

        return builder.build();

    }

}
