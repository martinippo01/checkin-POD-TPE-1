package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import checkin.CheckinServiceGrpc;
import checkin.CheckinServiceOuterClass.PassengerCheckinRequest;
import checkin.CheckinServiceOuterClass.PassengerCheckinResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import javax.naming.ServiceUnavailableException;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.checkin.CheckInArguments.*;

public final class PassengerCheckin extends CheckInAction {
    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;

    public PassengerCheckin(List<String> actionArguments) {
        super(actionArguments);
    }

    private PassengerCheckinRequest createRequest() {
        return PassengerCheckinRequest.newBuilder()
                .setBookingCode(arguments.get(BOOKING.getArgument()))
                .setSectorName(arguments.get(SECTOR.getArgument()))
                .setCounterNumber(Integer.parseInt(arguments.get(COUNTER.getArgument())))
                .build();
    }

    private PassengerCheckinResponse fetchResponse(PassengerCheckinRequest request) {
        return blockingStub.passengerCheckin(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServiceUnavailableException {
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        try {
            PassengerCheckinRequest request = createRequest();
            PassengerCheckinResponse response = fetchResponse(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServiceUnavailableException();
            }
        }
    }
}
