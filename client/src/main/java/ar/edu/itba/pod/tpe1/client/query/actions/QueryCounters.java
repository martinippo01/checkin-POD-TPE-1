package ar.edu.itba.pod.tpe1.client.query.actions;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import airport.CounterServiceOuterClass.*;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.query.CounterQueryAction;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.OUT_PATH;
import static ar.edu.itba.pod.tpe1.client.Arguments.SECTOR;

public final class QueryCounters extends CounterQueryAction {
    private CounterServiceGrpc.CounterServiceBlockingStub blockingStub;

    public QueryCounters(List<String> actionArguments, List<String> optionalActionArguments) {
        super(actionArguments, optionalActionArguments);
    }

    private void printCounterQueryResponse(QueryCountersResponse response, String outPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
            writer.write("Sector  Counters  Airline          Flights             People\n");
            writer.write("###############################################################\n");
            for (CounterInfo counter : response.getCountersList()) {
                String flights = String.join("|", counter.getFlightsList());
                flights = flights.isEmpty() ? "-" : flights;
                String airline = counter.getAirline().isEmpty() ? "-" : counter.getAirline();
                writer.write(String.format("   %-7s %-9s %-16s %-17s %-4d%n",
                        counter.getSector(),
                        counter.getRange(),
                        airline,
                        flights,
                        counter.getWaitingPeople()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CounterServiceGrpc.newBlockingStub(channel);

        try {
            QueryCountersRequest request = QueryCountersRequest.newBuilder()
                    .setSector(getArguments().get(SECTOR.getArgument()))
                    .build();
            QueryCountersResponse response = blockingStub.queryCounters(request);
            printCounterQueryResponse(response, getArguments().get(OUT_PATH.getArgument()));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.out.println("Sector  Counters  Airline          Flights             People");
                System.out.println("###############################################################");
            } else if (e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
                System.out.println("No counters found, please add counters to the sector. Optional -DoutPath= file skipped");
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
