package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.*;
import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.PassengerStatus.PASSENGER_STATUS_COUNTERS_NOT_ASSIGNED;
import static ar.edu.itba.pod.tpe1.PassengerStatus.PASSENGER_STATUS_INVALID_BOOKING_CODE;
import static ar.edu.itba.pod.tpe1.client.Arguments.BOOKING;

public final class PassengerStatus extends CheckInAction {
    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;

    public PassengerStatus(List<String> actionArguments) {
        super(actionArguments);
    }

    private PassengerStatusRequest createRequest() {
        return PassengerStatusRequest.newBuilder()
                .setBookingCode(getArguments().get(BOOKING.getArgument()))
                .build();
    }

    private PassengerStatusResponse fetchResponse(PassengerStatusRequest request) {
        return blockingStub.passengerStatus(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        try {
            PassengerStatusRequest request = createRequest();
            PassengerStatusResponse response = fetchResponse(request);

            switch (response.getStatus()) {
                case PASSENGER_STATUS_CHECKIN_ALREADY_DONE:
                    CounterInformation checkedInInfo = response.getData(0).getCheckedInCounter();
                    System.out.printf("Booking %s for flight %s from %s checked in at counter %d in Sector %s%n",
                            response.getBooking().getBookingCode(), response.getBooking().getFlightCode(),
                            response.getBooking().getAirlineName(), checkedInInfo.getCounter(), checkedInInfo.getSectorName());
                    break;
                case PASSENGER_STATUS_WAITING_FOR_CHECKIN:
                    CountersInformation waitingInfo = response.getData(0).getAvailableCounters();
                    CounterRange range = waitingInfo.getCounters();
                    int firstCounter = range.getFirstCounterNumber();
                    int lastCounter = firstCounter + range.getNumberOfConsecutiveCounters() - 1;
                    int queueLength = waitingInfo.getPeopleInQueue();
                    System.out.printf("Booking %s for flight %s from %s is now waiting to check-in on counters (%d-%d) in Sector %s with %d people in line%n",
                            response.getBooking().getBookingCode(), response.getBooking().getFlightCode(),
                            response.getBooking().getAirlineName(), firstCounter, lastCounter, waitingInfo.getSectorName(), queueLength);
                    break;
                case PASSENGER_STATUS_OUT_OF_QUEUE:
                    CountersInformation outOfQueueInfo = response.getData(0).getAvailableCounters();
                    CounterRange rangeOut = outOfQueueInfo.getCounters();
                    int firstCounterOut = rangeOut.getFirstCounterNumber();
                    int lastCounterOut = firstCounterOut + rangeOut.getNumberOfConsecutiveCounters() - 1;
                    System.out.printf("Booking %s for flight %s from %s can check-in on counters (%d-%d) in Sector %s%n",
                            response.getBooking().getBookingCode(), response.getBooking().getFlightCode(),
                            response.getBooking().getAirlineName(), firstCounterOut, lastCounterOut, outOfQueueInfo.getSectorName());
                    break;
                default:
                    if (response.getStatus().equals(PASSENGER_STATUS_INVALID_BOOKING_CODE)) {
                        System.out.println("Error: Invalid booking code.");
                    } else if (response.getStatus().equals(PASSENGER_STATUS_COUNTERS_NOT_ASSIGNED)) {
                        System.out.println("Error: No counters assigned that handle passengers from the specified booking.");
                    } else {
                        System.out.println("Error: An unknown error occurred.");
                    }
                    break;
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
