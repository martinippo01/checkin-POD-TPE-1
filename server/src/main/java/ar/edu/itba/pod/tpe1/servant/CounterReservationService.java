package ar.edu.itba.pod.tpe1.servant;

import airport.AirportService;
import counter.CounterReservationServiceGrpc;
import counter.CounterReservationServiceOuterClass;
import io.grpc.stub.StreamObserver;

public class CounterReservationService extends CounterReservationServiceGrpc.CounterReservationServiceImplBase{

    @Override
    public void listSectors(CounterReservationServiceOuterClass.SectorRequest request, StreamObserver<CounterReservationServiceOuterClass.SectorResponse> responseObserver) {
        CounterReservationServiceOuterClass.SectorResponse.Builder response = CounterReservationServiceOuterClass.SectorResponse.newBuilder();
        // Populate with example data
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCounterRange(CounterReservationServiceOuterClass.CounterRangeRequest request, StreamObserver<CounterReservationServiceOuterClass.CounterRangeResponse> responseObserver) {
        CounterReservationServiceOuterClass.CounterRangeResponse.Builder response = CounterReservationServiceOuterClass.CounterRangeResponse.newBuilder();
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void assignCounters(CounterReservationServiceOuterClass.AssignCounterRequest request, StreamObserver<CounterReservationServiceOuterClass.BasicResponse> responseObserver) {
        CounterReservationServiceOuterClass.BasicResponse.Builder response = CounterReservationServiceOuterClass.BasicResponse.newBuilder();
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void freeCounters(CounterReservationServiceOuterClass.FreeCounterRequest request, StreamObserver<CounterReservationServiceOuterClass.BasicResponse> responseObserver) {
        CounterReservationServiceOuterClass.BasicResponse.Builder response = CounterReservationServiceOuterClass.BasicResponse.newBuilder();
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void checkInCounters(CounterReservationServiceOuterClass.CheckInCounterRequest request, StreamObserver<CounterReservationServiceOuterClass.BasicResponse> responseObserver) {
        CounterReservationServiceOuterClass.BasicResponse.Builder response = CounterReservationServiceOuterClass.BasicResponse.newBuilder();
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listPendingAssignments(CounterReservationServiceOuterClass.PendingAssignmentsRequest request, StreamObserver<CounterReservationServiceOuterClass.PendingAssignmentsResponse> responseObserver) {
        CounterReservationServiceOuterClass.PendingAssignmentsResponse.Builder response = CounterReservationServiceOuterClass.PendingAssignmentsResponse.newBuilder();
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}