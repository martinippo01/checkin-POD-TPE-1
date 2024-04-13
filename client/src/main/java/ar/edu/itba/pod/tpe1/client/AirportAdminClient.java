package ar.edu.itba.pod.tpe1.client;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

    public void addPassengerManifest(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    AirportService.AddPassengerRequest request = AirportService.AddPassengerRequest.newBuilder()
                            .setBookingCode(parts[0])
                            .setFlightCode(parts[1])
                            .setAirlineName(parts[2])
                            .build();
                    AirportService.AddPassengerResponse response = blockingStub.addPassenger(request);
                    if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
                        System.out.println("Booking " + response.getBookingCode() + " added successfully");
                    } else {
                        System.out.println("Failed to add booking " + response.getBookingCode());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + filePath);
        }
    }


}
