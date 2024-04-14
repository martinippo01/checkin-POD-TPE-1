package ar.edu.itba.pod.tpe1.servant;

import airport.NotificationsServiceGrpc;
import airport.NotificationsServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.Notifications;
import ar.edu.itba.pod.tpe1.data.utils.Airline;
import ar.edu.itba.pod.tpe1.data.utils.Notification;
import io.grpc.stub.StreamObserver;

public class NotificationsServant extends NotificationsServiceGrpc.NotificationsServiceImplBase {

    private final Airport airport = Airport.getInstance();
    private final Notifications notifications = Notifications.getInstance();

    @Override
    public void registerNotifications(
            NotificationsServiceOuterClass.RegisterNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RegisterNotificationsResponse> responseObserver
    ){
        Airline airline = new Airline(req.getAirline());

        // First register the airline
        boolean success = notifications.registerAirline(airline);
        if (success){
            // TODO: evaluate cases, it can fail because the airline does not exist or there are no one waiting
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Failed").asRuntimeException());
        }

        // Stream the notification
        Notification notification;
        do{
            notification = notifications.getNotification(airline);
            if(notification != null){
                responseObserver.onNext(NotificationsServiceOuterClass.RegisterNotificationsResponse.newBuilder().build());
            }
        }while (notification != null); // null is the poison pill

        // Complete
        responseObserver.onCompleted();

    }

    @Override
    public void removeNotifications(
            NotificationsServiceOuterClass.RemoveNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RemoveNotificationsResponse> responseObserver
    ){
        Airline airline = new Airline(req.getAirline());

        boolean success = notifications.unregisterAirline(airline);
        if(success){
            responseObserver.onNext(NotificationsServiceOuterClass.RemoveNotificationsResponse.newBuilder().build());
            responseObserver.onCompleted();
        } else{
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Airline is not registered").asRuntimeException());
        }
    }

}
