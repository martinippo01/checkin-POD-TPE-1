package ar.edu.itba.pod.tpe1.servant;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.utils.RangeCounter;
import io.grpc.stub.StreamObserver;

import io.grpc.stub.StreamObserver;

public class AirportAdminServant extends AirportAdminServiceGrpc.AirportAdminServiceImplBase {

    private final Airport airport = Airport.getInstance();

    @Override
    public void addSector(AirportService.SectorRequest req, StreamObserver<AirportService.SectorResponse> responseObserver) {
        boolean success = airport.addSector(req.getSectorName());
        AirportService.ResponseStatus status = success ?
                AirportService.ResponseStatus.SUCCESS : AirportService.ResponseStatus.FAILURE;
        responseObserver.onNext(AirportService.SectorResponse.newBuilder()
                .setStatus(status)
                .setSectorName(req.getSectorName())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(AirportService.CounterRequest req, StreamObserver<AirportService.CounterResponse> responseObserver) {
        RangeCounter rangeCounter = airport.addCounters(req.getSectorName(), req.getCounterCount());
        if (rangeCounter == null) {
            responseObserver.onNext(AirportService.CounterResponse.newBuilder()
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .setSectorName(req.getSectorName())
                    .build());
        } else {
            responseObserver.onNext(AirportService.CounterResponse.newBuilder()
                    .setStatus(AirportService.ResponseStatus.SUCCESS)
                    .setSectorName(req.getSectorName())
                    .setFirstCounterId(rangeCounter.getCounterFrom())
                    .setLastCounterId(rangeCounter.getCounterTo())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addPassenger(AirportService.AddPassengerRequest req, StreamObserver<AirportService.AddPassengerResponse> responseObserver) {
        boolean success = airport.registerPassenger(req.getBookingCode(), req.getFlightCode(), req.getAirlineName());
        AirportService.ResponseStatus status = success ?
                AirportService.ResponseStatus.SUCCESS : AirportService.ResponseStatus.FAILURE;
        responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                .setBookingCode(req.getBookingCode())
                .setStatus(status)
                .build());
        responseObserver.onCompleted();
    }
}

