package ar.edu.itba.pod.tpe1.client;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class AirportAdminClient {
    private final AirportAdminServiceGrpc.AirportAdminServiceBlockingStub blockingStub;

    public AirportAdminClient(Channel channel) {
        blockingStub = AirportAdminServiceGrpc.newBlockingStub(channel);
    }

    public void addSector(String sectorName) {
        AirportService.SectorRequest request = AirportService.SectorRequest.newBuilder().setSectorName(sectorName).build();
        AirportService.SectorResponse response = blockingStub.addSector(request);
        if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
            System.out.println("Sector " + response.getSectorName() + " added successfully");
        } else {
            System.out.println("Failed to add sector: " + response.getSectorName());
        }
    }

    public void addCounters(String sectorName, int counterCount) {
        AirportService.CounterRequest request = AirportService.CounterRequest.newBuilder().setSectorName(sectorName).setCounterCount(counterCount).build();
        AirportService.CounterResponse response = blockingStub.addCounters(request);
        if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
            System.out.println(counterCount + " new counters (" + response.getFirstCounterId() + "-" + response.getLastCounterId() + ") in Sector " + response.getSectorName() + " added successfully");
        } else {
            System.out.println("Failed to add counters to sector: " + sectorName);
        }
    }

    public void addPassengerManifest(String manifestPath) {
        AirportService.ManifestRequest request = AirportService.ManifestRequest.newBuilder().setManifestPath(manifestPath).build();
        AirportService.ManifestResponse response = blockingStub.addPassengerManifest(request);
        if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
            for (AirportService.PassengerInfo info : response.getPassengersList()) {
                System.out.println("Booking " + info.getBookingCode() + " for " + info.getAirlineName() + " " + info.getFlightCode() + " added successfully");
            }
        } else {
            System.out.println("Failed to add manifest from: " + manifestPath);
        }
    }

}
