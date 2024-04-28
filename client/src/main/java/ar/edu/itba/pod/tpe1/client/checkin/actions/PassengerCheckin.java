package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.*;
import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class PassengerCheckin extends CheckInAction {
    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;

    public PassengerCheckin(List<String> actionArguments) {
        super(actionArguments);
    }

    private PassengerCheckinRequest createRequest() {
        return PassengerCheckinRequest.newBuilder()
                .setBookingCode(getArguments().get(BOOKING.getArgument()))
                .setSectorName(getArguments().get(SECTOR.getArgument()))
                .setCounterNumber(Integer.parseInt(getArguments().get(COUNTER.getArgument())))
                .build();
    }

    private PassengerCheckinResponse fetchResponse(PassengerCheckinRequest request) {
        return blockingStub.passengerCheckin(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        try {
            PassengerCheckinRequest request = createRequest();
            PassengerCheckinResponse response = fetchResponse(request);

            if (response.getStatus() == CheckinStatus.CHECKIN_STATUS_ADDED_TO_QUEUE) {
                BookingInformation bookingInfo = response.getBooking();
                CountersInformation countersInfo = response.getData();
                CounterRange range = countersInfo.getCounters();
                int firstCounter = range.getFirstCounterNumber();
                int lastCounter = firstCounter + range.getNumberOfConsecutiveCounters() - 1;
                String sector = countersInfo.getSectorName();
                int queueLength = countersInfo.getPeopleInQueue();

                System.out.printf("Booking %s for flight %s from %s is now waiting to check-in on counters (%d-%d) in Sector %s with %d people in line%n",
                        bookingInfo.getBookingCode(), bookingInfo.getFlightCode(), bookingInfo.getAirlineName(), firstCounter, lastCounter, sector, queueLength);
            } else {
                handleCheckinErrors(response.getStatus());
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                System.err.println(e.getMessage());
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            }
        }
    }

    private void handleCheckinErrors(CheckinStatus status) {
        switch (status) {
            case CHECKIN_STATUS_INVALID_BOOKING_CODE -> System.out.println("Error: Invalid booking code.");
            case CHECKIN_STATUS_INVALID_SECTOR_ID -> System.out.println("Error: Invalid sector ID.");
            case CHECKIN_STATUS_INVALID_COUNTER_NUMBER -> System.out.println("Error: Invalid counter number.");
            case CHECKIN_STATUS_PASSENGER_ALREADY_IN_QUEUE -> System.out.println("Error: Passenger already in queue.");
            case CHECKIN_STATUS_CHECKIN_ALREADY_DONE -> System.out.println("Error: Check-in already completed for this passenger.");
            default -> System.out.println("Error: An unknown error occurred.");
        }
    }

}
