package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.protos.CheckInService.*;
import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.BOOKING;

public final class FetchCounter extends CheckInAction {
    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;


    public FetchCounter(List<String> actionArguments) {
        super(actionArguments);
    }

    private FetchCounterRequest createRequest() {
        return FetchCounterRequest.newBuilder()
                .setBookingCode(getArguments().get(BOOKING.getArgument()))
                .build();
    }

    private FetchCounterResponse fetchResponse(FetchCounterRequest request) {
        return blockingStub.fetchCounter(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        try {
            FetchCounterRequest request = createRequest();
            FetchCounterResponse response = fetchResponse(request);

            if (response.getStatus() == CounterStatus.COUNTER_STATUS_COUNTERS_ASSIGNED) {
                BookingInformation bookingInfo = response.getBooking();
                CountersInformation countersInfo = response.getData(0);
                CounterRange range = countersInfo.getCounters();
                int firstCounter = range.getFirstCounterNumber();
                int lastCounter = firstCounter + range.getNumberOfConsecutiveCounters() - 1;
                String sector = countersInfo.getSectorName();
                int queueLength = countersInfo.getPeopleInQueue();

                System.out.printf("Flight %s from %s is now checking in at counters (%d-%d) in Sector %s with %d people in line%n",
                        bookingInfo.getFlightCode(), bookingInfo.getAirlineName(), firstCounter, lastCounter, sector, queueLength);
            } else if (response.getStatus() == CounterStatus.COUNTER_STATUS_COUNTERS_NOT_ASSIGNED) {
                BookingInformation bookingInfo = response.getBooking();
                System.out.printf("Flight %s from %s has no counters assigned yet%n",
                        bookingInfo.getFlightCode(), bookingInfo.getAirlineName());
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                System.err.println(e.getMessage());
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            }
        }
    }
}
