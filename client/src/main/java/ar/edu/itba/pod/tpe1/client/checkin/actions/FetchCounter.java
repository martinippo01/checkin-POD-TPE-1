package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import checkin.CheckinServiceGrpc;
import checkin.CheckinServiceOuterClass.FetchCounterRequest;
import checkin.CheckinServiceOuterClass.FetchCounterResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import javax.naming.ServiceUnavailableException;
import java.util.List;

import static ar.edu.itba.pod.tpe1.client.checkin.CheckInArguments.BOOKING;

public final class FetchCounter extends CheckInAction {
    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;


    public FetchCounter(List<String> actionArguments) {
        super(actionArguments);
    }

    private FetchCounterRequest createRequest() {
        return FetchCounterRequest.newBuilder()
                .setBookingCode(arguments.get(BOOKING.getArgument()))
                .build();
    }

    private FetchCounterResponse fetchResponse(FetchCounterRequest request) {
        return blockingStub.fetchCounter(request);
    }

    @Override
    public void run(ManagedChannel channel) throws ServiceUnavailableException {
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        try {
            FetchCounterRequest request = createRequest();
            FetchCounterResponse response = fetchResponse(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServiceUnavailableException();
            }
        }
    }
}
