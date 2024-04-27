package ar.edu.itba.pod.tpe1.servant;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.utils.RangeCounter;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class AirportAdminServant extends AirportAdminServiceGrpc.AirportAdminServiceImplBase {

    private final Airport airport = Airport.getInstance();


    @Override
    public void addSector(AirportService.SectorRequest req, StreamObserver<AirportService.SectorResponse> responseObserver) {
        try {
            airport.addSector(req.getSectorName());
            responseObserver.onNext(AirportService.SectorResponse.newBuilder()
                    .setSectorName(req.getSectorName())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void addCounters(AirportService.CounterRequest req, StreamObserver<AirportService.CounterResponse> responseObserver) {
        try {
            RangeCounter counter = airport.addCounters(req.getSectorName(), req.getCounterCount());
            responseObserver.onNext(AirportService.CounterResponse.newBuilder()
                    .setSectorName(req.getSectorName())
                            .setLastCounterId(counter.getCounterTo())
                            .setFirstCounterId(counter.getCounterFrom())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void addPassenger(AirportService.AddPassengerRequest req, StreamObserver<AirportService.AddPassengerResponse> responseObserver) {
        try {
            airport.registerPassenger(req.getBookingCode(), req.getFlightCode(), req.getAirlineName());
            responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                    .setBookingCode(req.getBookingCode())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }

    }
}

