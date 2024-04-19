package ar.edu.itba.pod.tpe1.client.admin;

import ar.edu.itba.pod.tpe1.client.Arguments;
import ar.edu.itba.pod.tpe1.client.Client;
import ar.edu.itba.pod.tpe1.client.Util;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static ar.edu.itba.pod.tpe1.client.admin.AirportAdminActions.airportAdminActionsFromString;

public class AirportAdminClient extends Client {
    private static final Logger logger = LoggerFactory.getLogger(AirportAdminClient.class);

    public AirportAdminClient(String[] args) {
        super(args);
    }

    @Override
    public void executeAction() throws ServerUnavailableException {

        AirportAdminActions action = airportAdminActionsFromString(getActionArgument()).orElseThrow(() -> {
                    logger.error("Provided action '{}' doesn't exist.", Arguments.SERVER_ADDRESS.getArgument());
                    return new IllegalArgumentException(Util.EXCEPTION_MESSAGE_UNEXPECTED_ARGUMENT + Arguments.ACTION.getArgument());
                }
        );

        action.getAction().setArguments(getArguments());
        action.getAction().run(getChannel());
    }

    // TODO: Move to specific actions
//    public void addSector(String sectorName) {
//        AirportService.SectorRequest request = AirportService.SectorRequest.newBuilder().setSectorName(sectorName).build();
//        AirportService.SectorResponse response = blockingStub.addSector(request);
//        if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
//            System.out.println("Sector " + response.getSectorName() + " added successfully");
//        } else {
//            System.out.println("Failed to add sector: " + response.getSectorName());
//        }
//    }
//
//    public void addCounters(String sectorName, int counterCount) {
//        AirportService.CounterRequest request = AirportService.CounterRequest.newBuilder().setSectorName(sectorName).setCounterCount(counterCount).build();
//        AirportService.CounterResponse response = blockingStub.addCounters(request);
//        if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
//            System.out.println(counterCount + " new counters (" + response.getFirstCounterId() + "-" + response.getLastCounterId() + ") in Sector " + response.getSectorName() + " added successfully");
//        } else {
//            System.out.println("Failed to add counters to sector: " + sectorName);
//        }
//    }
//
//    public void addPassengerManifest(String filePath) {
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            reader.readLine();  // This reads the headers like "booking;flight;airline" and does nothing with it
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split(";");
//                if (parts.length == 3) {
//                    AirportService.AddPassengerRequest request = AirportService.AddPassengerRequest.newBuilder()
//                            .setBookingCode(parts[0])
//                            .setFlightCode(parts[1])
//                            .setAirlineName(parts[2])
//                            .build();
//                    AirportService.AddPassengerResponse response = blockingStub.addPassenger(request);
//                    if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
//                        System.out.println("Booking " + response.getBookingCode() + " added successfully");
//                    } else {
//                        System.out.println("Failed to add booking " + response.getBookingCode());
//                    }
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Error reading the file: " + filePath);
//        }
//    }

    public static void main(String[] args) {
//        String[] test = {
//                "-DserverAddress=localhost:50058",
//                "-Daction=addSector",
//                "-Dsector=C"
//        };
//
//        String[] test2 = {
//                "-DserverAddress=localhost:50058",
//                "-Daction=addCounters",
//                "-Dsector=C",
//                "-Dcounters=3"
//        };

//        try (Client client = new AirportAdminClient(test2)) {
        try (Client client = new AirportAdminClient(args)) {
            client.executeAction();
        } catch (IllegalArgumentException e) {
            System.err.println(Util.ERROR_MESSAGE_INVALID_ARGUMENT);
            System.exit(1);
        } catch (ServerUnavailableException e) {
            System.err.println(Util.ERROR_MESSAGE_SERVER_UNAVAILABLE);
            System.exit(2);
        } catch (IOException e) {
            System.err.println(Util.ERROR_MESSAGE_IO);
            System.exit(3);
        }
    }

}
