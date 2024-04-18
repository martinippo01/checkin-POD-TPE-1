package ar.edu.itba.pod.tpe1.servant;

import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.CheckIn;
import checkin.CheckinServiceGrpc;
import checkin.CheckinServiceOuterClass;
import checkin.CheckinServiceOuterClass.CounterStatus;
import checkin.CheckinServiceOuterClass.CountersInformation;
import checkin.CheckinServiceOuterClass.FetchCounterRequest;
import checkin.CheckinServiceOuterClass.FetchCounterResponse;
import checkin.CheckinServiceOuterClass.BookingInformation;
import checkin.CheckinServiceOuterClass.PassengerCheckinRequest;
import checkin.CheckinServiceOuterClass.PassengerCheckinResponse;
import checkin.CheckinServiceOuterClass.PassengerStatusRequest;
import checkin.CheckinServiceOuterClass.PassengerStatusResponse;
import io.grpc.stub.StreamObserver;

public class CheckInServant extends CheckinServiceGrpc.CheckinServiceImplBase {
    private final CheckIn checkIn = CheckIn.getInstance();
    private final Airport airport = Airport.getInstance();

    @Override
    public void fetchCounter(FetchCounterRequest request, StreamObserver<FetchCounterResponse> responseObserver) {
        BookingInformation bookingInformation = airport.getBookingInformation(request.getBookingCode());

        FetchCounterResponse.Builder responseBuilder;
        if (bookingInformation == null) {
            // Return error
            responseBuilder = FetchCounterResponse
                    .newBuilder()
                    .setStatus(CounterStatus.COUNTER_STATUS_BOOKING_CODE_WITHOUT_AWAITING_PASSENGERS)
                    .setBooking(BookingInformation.newBuilder()
                            .setBookingCode(request.getBookingCode())
                            .build());
        } else {
            CountersInformation result = airport.countersByBooking(bookingInformation);

            responseBuilder = FetchCounterResponse
                    .newBuilder()
                    .setStatus(CounterStatus.COUNTER_STATUS_COUNTERS_ASSIGNED)
                    .setBooking(bookingInformation).setData(result);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void passengerCheckin(PassengerCheckinRequest request, StreamObserver<PassengerCheckinResponse> responseObserver) {
        super.passengerCheckin(request, responseObserver);
    }

    @Override
    public void passengerStatus(PassengerStatusRequest request, StreamObserver<PassengerStatusResponse> responseObserver) {
        super.passengerStatus(request, responseObserver);
    }
}
