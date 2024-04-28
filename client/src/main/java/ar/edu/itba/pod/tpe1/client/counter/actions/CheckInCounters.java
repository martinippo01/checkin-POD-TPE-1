package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.CheckInService.BookingInformation;
import ar.edu.itba.pod.tpe1.protos.CheckInService.CheckInCountersRequest;
import ar.edu.itba.pod.tpe1.protos.CheckInService.CheckInCountersResponse;
import ar.edu.itba.pod.tpe1.protos.CheckInService.CheckinServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class CheckInCounters extends CounterReservationAction {


    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;

    public CheckInCounters(List<String> actionArguments) {
        super(actionArguments);
    }

    private void printIdleCounter(int counter) {
        System.out.println("Counter " + counter + " is idle");
    }

    private void printCheckInInCounter(BookingInformation booking, int counter) {
        System.out.println("Check-in successful of " + booking.getBookingCode() + " for flight " + booking.getFlightCode() + " at counter " + counter);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());
        int fromVal = Integer.parseInt(getArguments().get(COUNTER_FROM.getArgument()));
        String airlineName = getArguments().get(AIRLINE.getArgument());

        CheckInCountersRequest request = CheckInCountersRequest.newBuilder()
                .setSectorName(sectorName)
                .setAirlineName(airlineName)
                .setCounterNumber(fromVal)
                .build();
        try {
            CheckInCountersResponse response = blockingStub.performCheckIn(request);
            response.getDataList().forEach(info -> {
                switch (info.getStatus()) {
                    case CHECK_IN_COUNTER_STATUS_IDLE -> printIdleCounter(info.getCounter());
                    case CHECK_IN_COUNTER_STATUS_SUCCESS -> printCheckInInCounter(info.getBooking(), info.getCounter());
                }
            });
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                System.err.println(e.getMessage());
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }

}
