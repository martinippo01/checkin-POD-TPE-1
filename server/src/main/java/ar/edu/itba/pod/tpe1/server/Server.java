package ar.edu.itba.pod.tpe1.server;

import ar.edu.itba.pod.tpe1.servant.*;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info(" Server Starting ...");

        int port = 50058;

        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(new AirportAdminServant())
                .addService(new CounterQueryServant())
                .addService(new PassengerCheckInServant())
                .addService(new NotificationsServant())
                .addService(new CounterReservationService())
                .build();
        server.start();
        logger.info("Server started, listening on {}", port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }
}
