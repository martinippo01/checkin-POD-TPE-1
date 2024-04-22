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

public class NotificationsServant extends NotificationsServiceGrpc.NotificationsServiceImplBase {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(NotificationsServant.class);
    // State
    private final Airport airport = Airport.getInstance();

    @Override
    public void registerNotifications(
            NotificationsServiceOuterClass.RegisterNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RegisterNotificationsResponse> responseObserver
    ){
        try {
            logger.info("Airline: {} requested register to notifications service", req.getAirline());
            Airline airline = new Airline(req.getAirline());

            // Check the airline exists
            if(!airport.airlineExists(airline)) {
                logger.error("Airline: {} failed to register to notifications service", airline.getName());
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Airline " + airline + " is not registered at the airport.").asRuntimeException());
                return;
            }

            // First register the airline
            try {
                airport.getNotificationsService().registerAirline(airline);
            }catch (IllegalArgumentException e){
                logger.error("Airline: {} failed to register to notifications service", airline.getName());
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
                return;
            }

            logger.info("Airline: {} successfully registered to notifications service", airline.getName());
            responseObserver.onNext(buildNotificationProto(new Notification.Builder().setNotificationType(NotificationsServiceOuterClass.NotificationType.SUCCESSFUL_REGISTER).build()));

            // Stream the notifications
            Notification notification;
            do {
                notification = airport.getNotificationsService().getNotification(airline);
                if (notification != null && !notification.isPoissonPill()) {
                    responseObserver.onNext(buildNotificationProto(notification));
                    logger.info("Sent notification of type {} to airline: {}", notification.getNotificationType(), airline.getName());
                }
            } while (notification != null && !notification.isPoissonPill()); // null is the poison pill - For when no more notifications need to be sent to the client

            // Once consumed the poisson pill, remove the airline from the airport
            airport.getNotificationsService().removeAirline(airline);

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
        boolean success = airport.getNotificationsService().unregisterAirline(airline);

        if(success){
            // In case the airline was registered, and now is not. Response goes empty
            responseObserver.onNext(NotificationsServiceOuterClass.RemoveNotificationsResponse.newBuilder().build());
            responseObserver.onCompleted();
            logger.info("Airline: {} successfully unregistered to notifications service", airline.getName());
        } else{
            // In case the airline was not registered, send the error.
            logger.error("Airline: {} failed to unregister to notifications service", airline.getName());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Airline is not registered").asRuntimeException());
        }
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
