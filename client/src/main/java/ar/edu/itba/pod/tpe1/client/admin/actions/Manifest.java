package ar.edu.itba.pod.tpe1.client.admin.actions;

import ar.edu.itba.pod.tpe1.client.admin.AirportAdminAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.AirportService.AddPassengerRequest;
import ar.edu.itba.pod.tpe1.protos.AirportService.AddPassengerResponse;
import ar.edu.itba.pod.tpe1.protos.AirportService.AirportAdminServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.IN_PATH;

public class Manifest extends AirportAdminAction {
    private AirportAdminServiceGrpc.AirportAdminServiceBlockingStub blockingStub;

    public Manifest(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = AirportAdminServiceGrpc.newBlockingStub(channel);

        String filePath = getArguments().get(IN_PATH.getArgument());

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine();  // This reads the headers like "booking;flight;airline" and does nothing with it
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    AddPassengerRequest request = AddPassengerRequest.newBuilder()
                            .setBookingCode(parts[0])
                            .setFlightCode(parts[1])
                            .setAirlineName(parts[2])
                            .build();
                    AddPassengerResponse response = blockingStub.addPassenger(request);

                    System.out.println("Booking " + response.getBookingCode() + " for " + parts[2] + " "+ parts[1] + " added successfully");
                }
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.err.println("Failed to add booking, not found");
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            } else {
                System.err.println("Failed to add booking: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + filePath);
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
