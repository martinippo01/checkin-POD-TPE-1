package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.CheckinServiceGrpc;
import ar.edu.itba.pod.tpe1.PassengerCheckinRequest;
import ar.edu.itba.pod.tpe1.PassengerCheckinResponse;
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
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServerUnavailableException();
            }
        }
    }
}
