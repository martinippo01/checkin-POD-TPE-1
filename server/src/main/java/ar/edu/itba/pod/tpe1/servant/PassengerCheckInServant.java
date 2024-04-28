package ar.edu.itba.pod.tpe1.servant;

import ar.edu.itba.pod.tpe1.CheckinServiceGrpc.CheckinServiceImplBase;
import ar.edu.itba.pod.tpe1.*;
import ar.edu.itba.pod.tpe1.data.Airport;
import counter.CounterReservationServiceOuterClass;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassengerCheckInServant extends CheckinServiceImplBase {

    private final Airport airport = Airport.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(PassengerCheckInServant.class);

    @Override
    public void fetchCounter(FetchCounterRequest request, StreamObserver<FetchCounterResponse> responseObserver) {
        try {
            logger.info("Fetching counter for booking code: {}", request.getBookingCode());
            FetchCounterResponse.Builder response = airport.listAssignedCounters(request.getBookingCode());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException fetching counter for booking code: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected Exception fetching counter for booking code: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void passengerCheckin(PassengerCheckinRequest request, StreamObserver<PassengerCheckinResponse> responseObserver) {
        try {
            logger.info("Adding passenger to check-in queue for booking code: {}", request.getBookingCode());
            PassengerCheckinResponse.Builder response = airport.addToCheckInQueue(request.getBookingCode(), request.getSectorName(), request.getCounterNumber());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException adding passenger to check-in queue for booking code: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            // TODO: check this
//        } catch (IllegalStateException e) {
//            responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected Exception adding passenger to check-in queue for booking code: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void passengerStatus(PassengerStatusRequest request, StreamObserver<PassengerStatusResponse> responseObserver) {
        try {
            logger.info("Checking passenger status for booking code: {}", request.getBookingCode());
            PassengerStatusResponse.Builder response = airport.getCheckIn(request.getBookingCode());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException checking passenger status for booking code: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected Exception checking passenger status for booking code: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void performCheckIn(CheckInCountersRequest request, StreamObserver<CheckInCountersResponse> responseObserver) {
        try{
            logger.info("Performing Check-In for Airline: {} at sector: {} from counter: {}", request.getAirlineName(), request.getSectorName(), request.getCounterNumber());
            CheckInCountersResponse.Builder responseBuilder = airport.performCheckIn(request.getSectorName(), request.getCounterNumber(), request.getAirlineName());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }catch(IllegalArgumentException e){
            logger.error("IllegalArgumentException performing Check-In for Airline: {} at sector: {} from counter: {}", request.getAirlineName(), request.getSectorName(), request.getCounterNumber());
        }catch(Exception e){
            logger.error("Unexpected Exception performing Check-In for Airline: {} at sector: {} from counter: {}", request.getAirlineName(), request.getSectorName(), request.getCounterNumber());
        }
    }
}
