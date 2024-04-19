package ar.edu.itba.pod.tpe1.client;

import ar.edu.itba.pod.tpe1.client.notifications.NotificationsClientMain;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationsTest {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    ManagedChannel channel;
    NotificationsClientMain notificationsClient;

    ManagedChannel channel2;
    NotificationsClientMain notificationsClient2;

    @Before
    public void setUp(){
        String[] args = {
                "-DserverAddress=localhost:50058", // -DserverAddress=10.6.0.1:50051
                "-Daction=register",
                "-Dairline=AmericanAirlines"
        };
        String[] args2 = {
                "-DserverAddress=localhost:50058", // -DserverAddress=10.6.0.1:50051
                "-Daction=unregister",
                "-Dairline=AmericanAirlines"
        };
        notificationsClient = new NotificationsClientMain(args);
        channel = notificationsClient.getChannel();

        notificationsClient2 = new NotificationsClientMain(args2);
        channel2 = notificationsClient2.getChannel();

//        channel = ManagedChannelBuilder.forAddress("localhost", 50058)
//                .usePlaintext()
//                .build();
//        notificationsClient = new NotificationsClient(channel);
    }

    @Test
    public void testRegisterAndUnregister() {


        // Thread 1
        Thread thread1 = new Thread(() -> {
            logger.info("Start register thread, register airline");
            notificationsClient.executeAction();
//            notificationsClient.registerNotifications("AmericanAirlines");
            logger.info("Airline registered");
        });

        // Thread 2
        Thread thread2 = new Thread(() -> {
            logger.info("Start unregister thread");
            try {
                logger.info("Sleep register thread");
                Thread.sleep(500); // Sleep for 5 seconds
                logger.info("Wake up register thread, try unregister airline");
                notificationsClient2.executeAction();
//                notificationsAction.unregisterNotifications("AmericanAirlines");
                logger.info("Airline unregistered");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start both threads
        logger.info("Start both threads");
        thread1.start();
        thread2.start();
    }
}
