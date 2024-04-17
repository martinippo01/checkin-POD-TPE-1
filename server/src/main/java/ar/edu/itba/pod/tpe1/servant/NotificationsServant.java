package ar.edu.itba.pod.tpe1.servant;

import airport.NotificationsServiceGrpc;
import airport.NotificationsServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.Notifications;
import ar.edu.itba.pod.tpe1.data.utils.Airline;
import ar.edu.itba.pod.tpe1.data.utils.Notification;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        logger.info("Airline: {} requested register to notifications service", req.getAirline());
        Airline airline = new Airline(req.getAirline());

        // First register the airline
        boolean success = notifications.registerAirline(airline);
        if (success){
            logger.info("Airline: {} successfully registered to notifications service", airline.getName());
            responseObserver.onNext(NotificationsServiceOuterClass.RegisterNotificationsResponse.newBuilder().setNotificationType(NotificationsServiceOuterClass.NotificationType.SUCCESSFUL_REGISTER).build());
        }else{
            // TODO: evaluate cases, it can fail because the airline does not exist or there are no one waiting
            // Mainly, evaluate the case when the airline exists, but there's no expected passengers
            logger.error("Airline: {} failed to register to notifications service", airline.getName());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Failed").asRuntimeException());
            return;
        }

        // Stream the notification
        Notification notification;
        do{
            notification = notifications.getNotification(airline);
            if(notification != null){
                responseObserver.onNext(buildNotificationProto(notification));
                logger.info("Sent notification of type {} to airline: {}", notification.getNotificationType(), airline.getName());
            }
        }while (notification != null); // null is the poison pill - For when no more notifications need to be sent to the client

        // Complete
        responseObserver.onCompleted();

    }

    @Override
    public void removeNotifications(
            NotificationsServiceOuterClass.RemoveNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RemoveNotificationsResponse> responseObserver
    ){
        Airline airline = new Airline(req.getAirline());

        logger.info("Airline: {} requested to be removed from notifications service", airline.getName());

        // Unregister the airline
        boolean success = notifications.unregisterAirline(airline);

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
        return
                NotificationsServiceOuterClass.RegisterNotificationsResponse.newBuilder()
                        .setNotificationType(notification.getNotificationType())
                        .setAirline(notification.getAirline().getName())
                        .setCounterFrom(notification.getCounterFrom())
                        .setCounterTo(notification.getCounterTo())
                        .setSector(notification.getSector())
                        .addAllFlights(notification.getFlights())
                        .setBooking(notification.getBooking())
                        .setFlight(notification.getFlight())
                        .setPeopleAhead(notification.getPeopleAhead())
                        .setCounter(notification.getCounter())
                        .setPendingAhead(notification.getPendingAhead())
                        .build();

    }

}
