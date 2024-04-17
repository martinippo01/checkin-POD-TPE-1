package ar.edu.itba.pod.tpe1.servant;

import airport.AirportService;
import airport.CounterServiceGrpc;
import counter.CounterReservationServiceGrpc;
import io.grpc.stub.StreamObserver;

public class CounterServiceImpl implements CounterReservationServiceGrpc.CounterReservationService {

    @Override
    public void listSectors(AirportService.SectorRequest req, StreamObserver<AirportService.SectorResponse> responseObserver) {
        AirportService.SectorResponse.Builder response = AirportService.SectorResponse.newBuilder();
        // Populate with example data
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCounterRange(CounterReservationService.CounterRangeRequest req, StreamObserver<CounterReservationService.CounterRangeResponse> responseObserver) {
        CounterReservationService.CounterRangeResponse.Builder response = CounterReservationService.CounterRangeResponse.newBuilder();
        // Example response setup for demonstration
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void assignCounters(CounterReservationService.AssignCounterRequest req, StreamObserver<CounterReservationService.BasicResponse> responseObserver) {
        CounterReservationService.BasicResponse.Builder response = CounterReservationService.BasicResponse.newBuilder().setMessage("Assignment completed or pending");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void freeCounters(CounterReservationService.FreeCounterRequest req, StreamObserver<CounterReservationService.BasicResponse> responseObserver) {
        CounterReservationService.BasicResponse.Builder response = CounterReservationService.BasicResponse.newBuilder().setMessage("Counters freed");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void checkInCounters(CounterReservationService.CheckInCounterRequest req, StreamObserver<CounterReservationService.BasicResponse> responseObserver) {
        CounterReservationService.BasicResponse.Builder response = CounterReservationService.BasicResponse.newBuilder().setMessage("Check-in processed");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listPendingAssignments(CounterReservationService.PendingAssignmentsRequest req, StreamObserver<CounterReservationService.PendingAssignmentsResponse> responseObserver) {
        CounterReservationService.PendingAssignmentsResponse.Builder response = CounterReservationService.PendingAssignmentsResponse.newBuilder();
        // Example pending assignments
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}

}