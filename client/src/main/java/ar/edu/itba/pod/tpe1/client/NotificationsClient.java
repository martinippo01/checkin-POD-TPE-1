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
        final String AIRLINE = "AerolineasArgentinas";
        NotificationsServiceOuterClass.RegisterNotificationsRequest request =
                NotificationsServiceOuterClass.RegisterNotificationsRequest
                        .newBuilder()
                        .setAirline(AIRLINE)
                        .build();
        Iterator<NotificationsServiceOuterClass.RegisterNotificationsResponse> response;

        // Make the request
        response = stub.registerNotifications(request);

        // Iterate the stream
        while (response.hasNext()){
            NotificationsServiceOuterClass.RegisterNotificationsResponse registerNotificationsResponse = response.next();
            System.out.println(registerNotificationsResponse.getNotificationType());
            System.out.println(registerNotificationsResponse);
        }

        NotificationsServiceOuterClass.RemoveNotificationsRequest removeNotificationsRequest =
                NotificationsServiceOuterClass.RemoveNotificationsRequest.newBuilder().setAirline(AIRLINE).build();
        NotificationsServiceOuterClass.RemoveNotificationsResponse removeNotificationsResponse =
                stub.removeNotifications(removeNotificationsRequest);

        System.out.println(removeNotificationsResponse);

//        // Now register twice to see error
//        System.out.println("We'll try to register the same airline twice");
//        stub.registerNotifications(request);
//        stub.registerNotifications(request);
//        stub.removeNotifications(removeNotificationsRequest);

        System.out.println("Connection with server closed");

    }

}
