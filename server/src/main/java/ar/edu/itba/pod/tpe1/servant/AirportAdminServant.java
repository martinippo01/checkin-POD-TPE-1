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
            responseObserver.onNext(AirportService.SectorResponse.newBuilder().setMessage("Sector already exists").build());
        } else {
            sectors.put(sectorName, 0);
            responseObserver.onNext(AirportService.SectorResponse.newBuilder().setMessage("Sector " + sectorName + " added successfully").build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(AirportService.CounterRequest req, StreamObserver<AirportService.CounterResponse> responseObserver) {
        String sectorName = req.getSectorName();
        int count = req.getCounterCount();
        if (!sectors.containsKey(sectorName)) {
            responseObserver.onNext(AirportService.CounterResponse.newBuilder().setMessage("Sector does not exist").build());
        } else {
            int firstId = counterId.getAndAdd(count);
            int lastId = firstId + count - 1;
            responseObserver.onNext(AirportService.CounterResponse.newBuilder().setMessage(count + " new counters (" + firstId + "-" + lastId + ") in Sector " + sectorName + " added successfully").build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addPassengerManifest(AirportService.ManifestRequest req, StreamObserver<AirportService.ManifestResponse> responseObserver) {
        // Logic to parse CSV and add passengers
        AirportService.ManifestResponse.Builder responseBuilder = AirportService.ManifestResponse.newBuilder();
        // Parse CSV file and add passenger statuses to responseBuilder
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
