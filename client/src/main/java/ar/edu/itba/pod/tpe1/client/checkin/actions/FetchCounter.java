package ar.edu.itba.pod.tpe1.client.checkin.actions;

import ar.edu.itba.pod.tpe1.client.checkin.CheckInAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.CheckInService.CheckinServiceGrpc;
import ar.edu.itba.pod.tpe1.protos.CheckInService.FetchCounterRequest;
import ar.edu.itba.pod.tpe1.protos.CheckInService.FetchCounterResponse;
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
        } catch (StatusRuntimeException e) {
            if (e.getStatus().equals(Status.INVALID_ARGUMENT)) {
                throw new IllegalArgumentException(e);
            } else if (e.getStatus().equals(Status.UNAVAILABLE)) {
                throw new ServerUnavailableException();
            }
        }
    }
}
