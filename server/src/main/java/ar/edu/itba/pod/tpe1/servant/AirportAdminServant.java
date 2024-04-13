package ar.edu.itba.pod.tpe1.servant;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportAdminServant extends AirportAdminServiceGrpc.AirportAdminServiceImplBase {

    private final ConcurrentHashMap<String, Integer> sectors = new ConcurrentHashMap<>();
    private final AtomicInteger counterId = new AtomicInteger(1);
    @Override
    public void addSector(AirportService.SectorRequest req, StreamObserver<AirportService.SectorResponse> responseObserver) {
        String sectorName = req.getSectorName();
        if (sectors.containsKey(sectorName)) {
            responseObserver.onNext(AirportService.SectorResponse.newBuilder().setStatus(AirportService.ResponseStatus.FAILURE).setSectorName(sectorName).build());
        } else {
            sectors.put(sectorName, 0);
            responseObserver.onNext(AirportService.SectorResponse.newBuilder().setStatus(AirportService.ResponseStatus.SUCCESS).setSectorName(sectorName).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(AirportService.CounterRequest req, StreamObserver<AirportService.CounterResponse> responseObserver) {
        String sectorName = req.getSectorName();
        int count = req.getCounterCount();
        if (!sectors.containsKey(sectorName)) {
            responseObserver.onNext(AirportService.CounterResponse.newBuilder().setStatus(AirportService.ResponseStatus.FAILURE).setSectorName(sectorName).build());
        } else {
            int firstId = counterId.getAndAdd(count);
            int lastId = firstId + count - 1;
            responseObserver.onNext(AirportService.CounterResponse.newBuilder().setStatus(AirportService.ResponseStatus.SUCCESS).setSectorName(sectorName).setFirstCounterId(firstId).setLastCounterId(lastId).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addPassenger(AirportService.AddPassengerRequest req, StreamObserver<AirportService.AddPassengerResponse> responseObserver) {
        // Example concurrency handling logic here
        AirportService.AddPassengerResponse response = AirportService.AddPassengerResponse.newBuilder()
                .setBookingCode(req.getBookingCode())
                .setStatus(AirportService.ResponseStatus.SUCCESS) // or FAILURE based on business logic
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
