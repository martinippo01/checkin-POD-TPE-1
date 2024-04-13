package ar.edu.itba.pod.tpe1.servant;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import ar.edu.itba.pod.tpe1.data.Airport;
import io.grpc.stub.StreamObserver;


public class AirportAdminServant extends AirportAdminServiceGrpc.AirportAdminServiceImplBase {

    Airport airport = Airport.getInstance();

    @Override
    public void addSector(AirportService.SectorRequest req, StreamObserver<AirportService.SectorResponse> responseObserver) {
        String sectorName = req.getSectorName();
        if (airport.containsSector(sectorName)) {
            responseObserver.onNext(AirportService.SectorResponse.newBuilder()
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .setSectorName(sectorName)
                    .build());
        } else {
            airport.putSector(sectorName, 0);
            responseObserver.onNext(AirportService.SectorResponse.newBuilder()
                    .setStatus(AirportService.ResponseStatus.SUCCESS)
                    .setSectorName(sectorName)
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(AirportService.CounterRequest req, StreamObserver<AirportService.CounterResponse> responseObserver) {
        String sectorName = req.getSectorName();
        int count = req.getCounterCount();
        if(count <= 0) {
            responseObserver.onNext(AirportService.CounterResponse.newBuilder()
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .setSectorName(sectorName)
                    .build());
        } else if (!airport.containsSector(sectorName)) {
            responseObserver.onNext(AirportService.CounterResponse.newBuilder()
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .setSectorName(sectorName)
                    .build());
        } else {
            int firstId = airport.addCounters(count);
            int lastId = firstId + count - 1;
            responseObserver.onNext(AirportService.CounterResponse.newBuilder()
                    .setStatus(AirportService.ResponseStatus.SUCCESS)
                    .setSectorName(sectorName)
                    .setFirstCounterId(firstId)
                    .setLastCounterId(lastId)
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addPassenger(AirportService.AddPassengerRequest req, StreamObserver<AirportService.AddPassengerResponse> responseObserver) {
        String bookingCode = req.getBookingCode();
        String flightCode = req.getFlightCode();
        String airlineName = req.getAirlineName();

        if (!airport.addBookingCode(bookingCode)) {
            responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                    .setBookingCode(bookingCode)
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .build());
            responseObserver.onCompleted();
            return;
        }

        if (!airport.setFlightToAirline(flightCode, airlineName)) {
            airport.removeBookingCode(bookingCode); // Clean up booking code since addition failed
            responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                    .setBookingCode(bookingCode)
                    .setStatus(AirportService.ResponseStatus.FAILURE)
                    .build());
        } else {
            responseObserver.onNext(AirportService.AddPassengerResponse.newBuilder()
                    .setBookingCode(bookingCode)
                    .setStatus(AirportService.ResponseStatus.SUCCESS)
                    .build());
        }
        responseObserver.onCompleted();
    }
}
