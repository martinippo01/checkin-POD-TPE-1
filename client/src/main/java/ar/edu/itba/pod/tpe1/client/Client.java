package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

//    public static void main(String[] args) throws InterruptedException {
//        logger.info("grpc-com-tpe1 Client Starting ...");
//        logger.info("grpc-com-patterns Client Starting ...");
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50058)
//                .usePlaintext()
//                .build();
//        try {
//            //AirportAdminClient airportAdminClient = new AirportAdminClient(channel);
//            NotificationsClient notificationsClient = new NotificationsClient(channel);
//            notificationsClient.registerNotifications("AmericanAirlines");
//            notificationsClient.unregisterNotifications("AmericanAirlines");
//        } finally {
//            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
//        }
//    }


        public static void main(String[] args) {

            logger.info("grpc-com-tpe1 Client Starting ...");
            logger.info("grpc-com-patterns Client Starting ...");
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();
            NotificationsClient notificationsClient = new NotificationsClient(channel);
            // Thread 1
            Thread thread1 = new Thread(() -> {
                System.out.println("Thread 1 started");
                try {
                    Thread.sleep(100); // Sleep

                    notificationsClient.registerNotifications("AmericanAirlines");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 1 completed");
            });

            // Thread 2
            Thread thread2 = new Thread(() -> {
                System.out.println("Thread 2 started");
                try {
                    Thread.sleep(5000); // Sleep for 5 seconds
                    notificationsClient.unregisterNotifications("AmericanAirlines");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread 2 completed");
            });

            // Start both threads
            thread1.start();
            thread2.start();
        }


}
