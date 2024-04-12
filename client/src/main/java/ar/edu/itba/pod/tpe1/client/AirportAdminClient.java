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
        System.out.println(response.getMessage());
    }

    public void addCounters(String sectorName, int counterCount) {
        AirportService.CounterRequest request = AirportService.CounterRequest.newBuilder().setSectorName(sectorName).setCounterCount(counterCount).build();
        AirportService.CounterResponse response = blockingStub.addCounters(request);
        System.out.println(response.getMessage());
    }

    public void addPassengerManifest(String manifestPath) {
        AirportService.ManifestRequest request = AirportService.ManifestRequest.newBuilder().setManifestPath(manifestPath).build();
        AirportService.ManifestResponse response = blockingStub.addPassengerManifest(request);
        for (AirportService.PassengerStatus status : response.getPassengersList()) {
            System.out.println(status.getMessage());
        }
    }

    public static void main(String[] args) {
        // Parse arguments and invoke methods accordingly
    }
}
