package ar.edu.itba.pod.tpe1.client;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import io.grpc.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AirportAdminClient {
    private final AirportAdminServiceGrpc.AirportAdminServiceBlockingStub blockingStub;

    public AirportAdminClient(Channel channel) {
        blockingStub = AirportAdminServiceGrpc.newBlockingStub(channel);
    }

    public void addSector(String sectorName) {
        try {
            AirportService.SectorRequest request = AirportService.SectorRequest.newBuilder().setSectorName(sectorName).build();
            AirportService.SectorResponse response = blockingStub.addSector(request);
            System.out.println("Sector " + response.getSectorName() + " added successfully");
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.out.println("Failed to add sector: ");
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }

    public void addCounters(String sectorName, int counterCount) {
        try {
            AirportService.CounterRequest request = AirportService.CounterRequest.newBuilder().setSectorName(sectorName).setCounterCount(counterCount).build();
            AirportService.CounterResponse response = blockingStub.addCounters(request);
            System.out.println(counterCount + " new counters (" + response.getFirstCounterId() + "-" + response.getLastCounterId() + ") in Sector " + response.getSectorName() + " added successfully");
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
                System.out.println("Failed to add counters to sector: " + sectorName);
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }

    public void addPassengerManifest(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine();  // This reads the headers like "booking;flight;airline" and does nothing with it
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    try {
                    AirportService.AddPassengerRequest request = AirportService.AddPassengerRequest.newBuilder()
                            .setBookingCode(parts[0])
                            .setFlightCode(parts[1])
                            .setAirlineName(parts[2])
                            .build();
                    AirportService.AddPassengerResponse response = blockingStub.addPassenger(request);
                    System.out.println("Booking " + response.getBookingCode() + " added successfully");
                    } catch (StatusRuntimeException e) {
                        if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                            System.out.println("Failed to add counters to sector: Booking already exists");
                        } else if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                            System.out.println("Failed to add booking: Failed registered to another airline");
                        }
                    } catch (Exception e) {
                        System.err.println("RPC failed: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + filePath);
        }
    }


}
