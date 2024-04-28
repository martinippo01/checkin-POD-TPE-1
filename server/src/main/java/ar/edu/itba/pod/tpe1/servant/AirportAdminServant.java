package ar.edu.itba.pod.tpe1.servant;

import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.utils.RangeCounter;
import ar.edu.itba.pod.tpe1.protos.AirportService.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirportAdminServant extends AirportAdminServiceGrpc.AirportAdminServiceImplBase {

    private final Airport airport = Airport.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(AirportAdminServant.class);

    @Override
    public void addSector(SectorRequest req, StreamObserver<SectorResponse> responseObserver) {
        try {
            logger.info("Adding sector: {}", req.getSectorName());
            airport.addSector(req.getSectorName());
            responseObserver.onNext(SectorResponse.newBuilder()
                    .setSectorName(req.getSectorName())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException adding sector: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected Exception adding sector: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void addCounters(CounterRequest req, StreamObserver<CounterResponse> responseObserver) {
        try {
            logger.info("Adding counters to sector: {}", req.getSectorName());
            RangeCounter counter = airport.addCounters(req.getSectorName(), req.getCounterCount());
            responseObserver.onNext(CounterResponse.newBuilder()
                    .setSectorName(req.getSectorName())
                    .setLastCounterId(counter.getCounterTo())
                    .setFirstCounterId(counter.getCounterFrom())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException adding counters to sector: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected Exception adding counters to sector: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void addPassenger(AddPassengerRequest req, StreamObserver<AddPassengerResponse> responseObserver) {
        try {
            logger.info("Adding passenger to flight: {}", req.getFlightCode());
            airport.registerPassenger(req.getBookingCode(), req.getFlightCode(), req.getAirlineName());
            responseObserver.onNext(AddPassengerResponse.newBuilder()
                    .setBookingCode(req.getBookingCode())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException adding passenger to flight: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected Exception adding passenger to flight: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }

    }
}

