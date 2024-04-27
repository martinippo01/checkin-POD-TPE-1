package ar.edu.itba.pod.tpe1.servant;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class CounterQueryServant extends CounterServiceGrpc.CounterServiceImplBase {

    private final Airport airport = Airport.getInstance();

    @Override
    public void queryCounters(CounterServiceOuterClass.QueryCountersRequest req, StreamObserver<CounterServiceOuterClass.QueryCountersResponse> responseObserver) {
        try {
            List<CounterServiceOuterClass.CounterInfo> results = airport.queryCountersBySector(req.getSector());

            CounterServiceOuterClass.QueryCountersResponse.Builder responseBuilder = CounterServiceOuterClass.QueryCountersResponse
                    .newBuilder()
                    .addAllCounters(results);

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalStateException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void queryCheckIns(CounterServiceOuterClass.QueryCheckInsRequest req, StreamObserver<CounterServiceOuterClass.QueryCheckInsResponse> responseObserver) {

        List<CounterServiceOuterClass.CheckInRecord> results;
        try {
            results = airport.queryCheckIns(req.getSector(), req.getAirline());
        }catch (IllegalArgumentException e){
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
            return;
        }

        CounterServiceOuterClass.QueryCheckInsResponse.Builder responseBuilder
                = CounterServiceOuterClass.QueryCheckInsResponse
                    .newBuilder()
                    .addAllCheckIns(results);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
