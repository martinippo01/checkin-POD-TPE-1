package ar.edu.itba.pod.tpe1.servant;

import ar.edu.itba.pod.tpe1.CheckinServiceGrpc.CheckinServiceImplBase;
import ar.edu.itba.pod.tpe1.*;
import ar.edu.itba.pod.tpe1.data.Airport;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class PassengerCheckInServant extends CheckinServiceImplBase {

    private final Airport airport = Airport.getInstance();

    @Override
    public void fetchCounter(FetchCounterRequest request, StreamObserver<FetchCounterResponse> responseObserver) {
        try {
            FetchCounterResponse.Builder response = airport.listAssignedCounters(request.getBookingCode());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void passengerCheckin(PassengerCheckinRequest request, StreamObserver<PassengerCheckinResponse> responseObserver) {
        try {
            PassengerCheckinResponse.Builder response = airport.addToCheckInQueue(request.getBookingCode(), request.getSectorName(), request.getCounterNumber());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            // TODO: check this
//        } catch (IllegalStateException e) {
//            responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void passengerStatus(PassengerStatusRequest request, StreamObserver<PassengerStatusResponse> responseObserver) {
        try {
            PassengerStatusResponse.Builder response = airport.getCheckIn(request.getBookingCode());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
