package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import checkin.CheckinServiceGrpc;
import checkin.CheckinServiceOuterClass.PassengerStatusRequest;
import checkin.CheckinServiceOuterClass.PassengerStatusResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import javax.naming.ServiceUnavailableException;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.checkin.CheckInArguments.*;

public final class PassengerStatus extends CheckInAction {
    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;

    public PassengerStatus(List<String> actionArguments) {
        super(actionArguments);
    }

    private PassengerStatusRequest createRequest() {
        return PassengerStatusRequest.newBuilder()
                .setBookingCode(arguments.get(BOOKING.getArgument()))
                .setSectorName(arguments.get(SECTOR.getArgument()))
                .setCounterNumber(Integer.parseInt(arguments.get(COUNTER.getArgument())))
                .build();
    }

    private PassengerStatusResponse fetchResponse(PassengerStatusRequest request) {
        return blockingStub.passengerStatus(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServiceUnavailableException {
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        try {
            PassengerStatusRequest request = createRequest();
            PassengerStatusResponse response = fetchResponse(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServiceUnavailableException();
            }
        }
    }
}
