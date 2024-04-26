package ar.edu.itba.pod.tpe1.servant;

import ar.edu.itba.pod.tpe1.CheckinServiceGrpc.CheckinServiceImplBase;
import ar.edu.itba.pod.tpe1.*;
import ar.edu.itba.pod.tpe1.data.Airport;
import io.grpc.stub.StreamObserver;

public class PassengerCheckInServant extends CheckinServiceImplBase {

    private final Airport airport = Airport.getInstance();

    @Override
    public void fetchCounter(FetchCounterRequest request, StreamObserver<FetchCounterResponse> responseObserver) {
        FetchCounterResponse.Builder response = airport.listAssignedCounters(request.getBookingCode());

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void passengerCheckin(PassengerCheckinRequest request, StreamObserver<PassengerCheckinResponse> responseObserver) {
        PassengerCheckinResponse.Builder response = airport.addToCheckInQueue(request.getBookingCode(), request.getSectorName(), request.getCounterNumber());

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void passengerStatus(PassengerStatusRequest request, StreamObserver<PassengerStatusResponse> responseObserver) {
        PassengerStatusResponse.Builder response = airport.getCheckIn(request.getBookingCode());

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
