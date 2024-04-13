package ar.edu.itba.pod.tpe1.servant;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import ar.edu.itba.pod.tpe1.data.Passenger;
import io.grpc.stub.StreamObserver;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportAdminServant extends AirportAdminServiceGrpc.AirportAdminServiceImplBase {

    private final ConcurrentHashMap<String, String> flightToAirlineMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> bookingCodes = new ConcurrentHashMap<>();

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
        if(count <= 0) {
            responseObserver.onNext(AirportService.CounterResponse.newBuilder().setStatus(AirportService.ResponseStatus.FAILURE).setSectorName(sectorName).build());
            responseObserver.onCompleted();
        } else if (!sectors.containsKey(sectorName)) {
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
        String bookingCode = req.getBookingCode();
        String flightCode = req.getFlightCode();
        String airlineName = req.getAirlineName();

        Integer newBooking = bookingCodes.putIfAbsent(bookingCode, 1);
        if (newBooking != null) {
            responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                    .setBookingCode(bookingCode)
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .build());
            responseObserver.onCompleted();
            return;
        }

        String existingAirline = flightToAirlineMap.putIfAbsent(flightCode, airlineName);
        if (existingAirline != null && !existingAirline.equals(airlineName)) {
            responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                    .setBookingCode(bookingCode)
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .build());
            bookingCodes.remove(bookingCode); // Clean up booking code since addition failed
        } else {
            responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                    .setBookingCode(bookingCode)
                    .setStatus(AirportService.ResponseStatus.SUCCESS)
                    .build());
        }
        responseObserver.onCompleted();
    }

}
