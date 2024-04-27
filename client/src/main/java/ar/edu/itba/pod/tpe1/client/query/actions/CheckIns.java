package ar.edu.itba.pod.tpe1.client.query.actions;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.client.query.CounterQueryAction;
import io.grpc.ManagedChannel;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.AIRLINE;
import static ar.edu.itba.pod.tpe1.client.Arguments.SECTOR;

public final class CheckIns extends CounterQueryAction {
    private CounterServiceGrpc.CounterServiceBlockingStub blockingStub;

    public CheckIns(List<String> actionArguments, List<String> optionalActionArguments) {
        super(actionArguments, optionalActionArguments);
    }

    private void printCheckInsQueryResponse(CounterServiceOuterClass.QueryCheckInsResponse response) {
        System.out.println("   Sector  Counter   Airline           Flight     Booking");
        System.out.println("   ###############################################################");
        for (CounterServiceOuterClass.CheckInRecord record : response.getCheckInsList()) {
            System.out.printf("   %-7s %-9d %-17s %-9s %-6s%n",
                    record.getSector(),
                    record.getCounter(),
                    record.getAirline(),
                    record.getFlight(),
                    record.getBookingCode());
        }
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CounterServiceGrpc.newBlockingStub(channel);

        CounterServiceOuterClass.QueryCheckInsRequest request = CounterServiceOuterClass.QueryCheckInsRequest.newBuilder()
                .setSector(getArguments().get(SECTOR.getArgument()))
                .setAirline(getArguments().get(AIRLINE.getArgument()))
                .build();
        CounterServiceOuterClass.QueryCheckInsResponse response = blockingStub.queryCheckIns(request);
        if (response.getCheckInsCount() == 0) {
            System.out.println("No check-ins found for the specified criteria.");
            return;
        }
        printCheckInsQueryResponse(response);
    }
}
