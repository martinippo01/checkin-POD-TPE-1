package ar.edu.itba.pod.tpe1.client.query.actions;

import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.query.CounterQueryAction;
import ar.edu.itba.pod.tpe1.protos.CounterService.CheckInRecord;
import ar.edu.itba.pod.tpe1.protos.CounterService.CounterServiceGrpc;
import ar.edu.itba.pod.tpe1.protos.CounterService.QueryCheckInsRequest;
import ar.edu.itba.pod.tpe1.protos.CounterService.QueryCheckInsResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class CheckIns extends CounterQueryAction {

    public CheckIns(List<String> actionArguments, List<String> optionalActionArguments) {
        super(actionArguments, optionalActionArguments);
    }

    private void printCheckInsQueryResponse(QueryCheckInsResponse response, String outPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
            writer.write("   Sector  Counter   Airline           Flight     Booking\n");
            writer.write("   ###############################################################\n");
            for (CheckInRecord record : response.getCheckInsList()) {
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

        try {
            QueryCheckInsRequest request = QueryCheckInsRequest.newBuilder()
                    .setSector(getArguments().getOrDefault(SECTOR.getArgument(), ""))
                    .setAirline(getArguments().getOrDefault(AIRLINE.getArgument(), ""))
                    .build();
            QueryCheckInsResponse response = blockingStub.queryCheckIns(request);
            if (response.getCheckInsCount() == 0) {
                System.out.println("No check-ins found for the specified criteria.");
                return;
            }
            printCheckInsQueryResponse(response, getArguments().get(OUT_PATH.getArgument()));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
