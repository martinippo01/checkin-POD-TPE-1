package ar.edu.itba.pod.tpe1.servant;

import airport.NotificationsServiceGrpc;
import airport.NotificationsServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.Notifications;
import io.grpc.stub.StreamObserver;

public class NotificationsServant extends NotificationsServiceGrpc.NotificationsServiceImplBase {

    private final Airport airport = Airport.getInstance();
    private final Notifications notifications = Notifications.getInstance();

    @Override
    public void registerNotifications(
            NotificationsServiceOuterClass.RegisterNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RegisterNotificationsResponse> responseObserver
    ){

    }

    @Override
    public void removeNotifications(
            NotificationsServiceOuterClass.RemoveNotificationsRequest req,
            StreamObserver<NotificationsServiceOuterClass.RemoveNotificationsResponse> responseObserver
    ){

    }

}
