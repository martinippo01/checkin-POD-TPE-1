package ar.edu.itba.pod.tpe1.client.query.actions;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.query.CounterQueryAction;
import io.grpc.ManagedChannel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class CheckIns extends CounterQueryAction {

    public CheckIns(List<String> actionArguments, List<String> optionalActionArguments) {
        super(actionArguments, optionalActionArguments);
    }

    private void printCheckInsQueryResponse(CounterServiceOuterClass.QueryCheckInsResponse response, String outPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
            writer.write("   Sector  Counter   Airline           Flight     Booking\n");
            writer.write("   ###############################################################\n");
            for (CounterServiceOuterClass.CheckInRecord record : response.getCheckInsList()) {
                writer.write(String.format("   %-7s %-9d %-17s %-9s %-6s%n",
                        record.getSector(),
                        record.getCounter(),
                        record.getAirline(),
                        record.getFlight(),
                        record.getBookingCode()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        CounterServiceGrpc.CounterServiceBlockingStub blockingStub = CounterServiceGrpc.newBlockingStub(channel);

        CounterServiceOuterClass.QueryCheckInsRequest request = CounterServiceOuterClass.QueryCheckInsRequest.newBuilder()
                .setSector(getArguments().get(SECTOR.getArgument()))
                .setAirline(getArguments().get(AIRLINE.getArgument()))
                .build();
        CounterServiceOuterClass.QueryCheckInsResponse response = blockingStub.queryCheckIns(request);
        if (response.getCheckInsCount() == 0) {
            System.out.println("No check-ins found for the specified criteria.");
            return;
        }
        printCheckInsQueryResponse(response, getArguments().get(OUT_PATH.getArgument()));
    }
}
