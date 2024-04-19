package ar.edu.itba.pod.tpe1.client.query;
import io.grpc.Channel;
import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass.QueryCountersRequest;
import airport.CounterServiceOuterClass.QueryCountersResponse;
import airport.CounterServiceOuterClass.QueryCheckInsRequest;
import airport.CounterServiceOuterClass.QueryCheckInsResponse;
import airport.CounterServiceOuterClass.CounterInfo;
import airport.CounterServiceOuterClass.CheckInRecord;

public class CounterQueryClient {
    private final CounterServiceGrpc.CounterServiceBlockingStub blockingStub;

    public CounterQueryClient(Channel channel) {
        blockingStub = CounterServiceGrpc.newBlockingStub(channel);
    }

    public void queryCounters(String sector) {
        QueryCountersRequest request = QueryCountersRequest.newBuilder()
                .setSector(sector)
                .build();
        QueryCountersResponse response = blockingStub.queryCounters(request);
        if (response.getCountersCount() == 0) {
            System.out.println("No counters found for the specified sector.");
            return;
        }
        printCounterQueryResponse(response);
    }

    private void printCounterQueryResponse(QueryCountersResponse response) {
        System.out.println("   Sector  Counters  Airline          Flights             People");
        System.out.println("   ###############################################################");
        for (CounterInfo counter : response.getCountersList()) {
            String flights = String.join("|", counter.getFlightsList());
            if (flights.isEmpty()) flights = "-";
            String airline = counter.getAirline().isEmpty() ? "-" : counter.getAirline();
            System.out.printf("   %-7s %-9s %-16s %-17s %-4d%n",
                    counter.getSector(),
                    "(" + counter.getRange() + ")",
                    airline,
                    flights,
                    counter.getWaitingPeople());
        }
    }

    public void queryCheckIns(String sector, String airline) {
        QueryCheckInsRequest request = QueryCheckInsRequest.newBuilder()
                .setSector(sector)
                .setAirline(airline)
                .build();
        QueryCheckInsResponse response = blockingStub.queryCheckIns(request);
        if (response.getCheckInsCount() == 0) {
            System.out.println("No check-ins found for the specified criteria.");
            return;
        }
        printCheckInsQueryResponse(response);
    }

    private void printCheckInsQueryResponse(QueryCheckInsResponse response) {
        System.out.println("   Sector  Counter   Airline           Flight     Booking");
        System.out.println("   ###############################################################");
        for (CheckInRecord record : response.getCheckInsList()) {
            System.out.printf("   %-7s %-9d %-17s %-9s %-6s%n",
                    record.getSector(),
                    record.getCounter(),
                    record.getAirline(),
                    record.getFlight(),
                    record.getBookingCode());
        }
    }
}
