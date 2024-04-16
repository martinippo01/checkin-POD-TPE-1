package ar.edu.itba.pod.tpe1.client;


import airport.NotificationsServiceGrpc;

import airport.NotificationsServiceOuterClass;
import io.grpc.Channel;

import java.util.Iterator;

public class NotificationsClient {

    private final NotificationsServiceGrpc.NotificationsServiceBlockingStub stub;

    public NotificationsClient(Channel channel){
        this.stub = NotificationsServiceGrpc.newBlockingStub(channel);
    }

    public void run (){
        final String AIRLINE = "Aerolineas Argentinas";
        NotificationsServiceOuterClass.RegisterNotificationsRequest request =
                NotificationsServiceOuterClass.RegisterNotificationsRequest
                        .newBuilder()
                        .setAirline(AIRLINE)
                        .build();
        Iterator<NotificationsServiceOuterClass.RegisterNotificationsResponse> response;
        response = stub.registerNotifications(request);

        while (response.hasNext()){
            NotificationsServiceOuterClass.RegisterNotificationsResponse registerNotificationsResponse = response.next();
            System.out.println(registerNotificationsResponse.getNotificationType());
        }

        NotificationsServiceOuterClass.RemoveNotificationsRequest removeNotificationsRequest =
                NotificationsServiceOuterClass.RemoveNotificationsRequest.newBuilder().setAirline(AIRLINE).build();
        NotificationsServiceOuterClass.RemoveNotificationsResponse removeNotificationsResponse =
                stub.removeNotifications(removeNotificationsRequest);

        System.out.println(removeNotificationsResponse);

        System.out.println("Connection with server closed");

    }

}
