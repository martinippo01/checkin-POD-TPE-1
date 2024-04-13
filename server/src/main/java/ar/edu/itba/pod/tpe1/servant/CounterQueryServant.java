package ar.edu.itba.pod.tpe1.servant;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import io.grpc.stub.StreamObserver;

public class CounterQueryServant extends CounterServiceGrpc.CounterServiceImplBase {
    @Override
    public void queryCounters(CounterServiceOuterClass.QueryCountersRequest req, StreamObserver<CounterServiceOuterClass.QueryCountersResponse> responseObserver) {
        CounterServiceOuterClass.QueryCountersResponse.Builder responseBuilder = CounterServiceOuterClass.QueryCountersResponse.newBuilder();
        // Implement logic to populate response based on the sector filtering
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCheckIns(CounterServiceOuterClass.QueryCheckInsRequest req, StreamObserver<CounterServiceOuterClass.QueryCheckInsResponse> responseObserver) {
        CounterServiceOuterClass.QueryCheckInsResponse.Builder responseBuilder = CounterServiceOuterClass.QueryCheckInsResponse.newBuilder();
        // Implement logic to populate response based on sector and airline filtering
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
