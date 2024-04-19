package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import checkin.CheckinServiceGrpc;
import checkin.CheckinServiceOuterClass.PassengerStatusRequest;
import checkin.CheckinServiceOuterClass.PassengerStatusResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class PassengerStatus extends CheckInAction {
    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;

    public PassengerStatus(List<String> actionArguments) {
        super(actionArguments);
    }

    private PassengerStatusRequest createRequest() {
        return PassengerStatusRequest.newBuilder()
                .setBookingCode(getArguments().get(BOOKING.getArgument()))
                .setSectorName(getArguments().get(SECTOR.getArgument()))
                .setCounterNumber(Integer.parseInt(getArguments().get(COUNTER.getArgument())))
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
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServerUnavailableException();
            }
        }
    }
}
