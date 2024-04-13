package ar.edu.itba.pod.tpe1.servant;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class CounterQueryServant extends CounterServiceGrpc.CounterServiceImplBase {

    private final Airport airport = Airport.getInstance();

    @Override
    public void queryCounters(CounterServiceOuterClass.QueryCountersRequest req, StreamObserver<CounterServiceOuterClass.QueryCountersResponse> responseObserver) {
        List<CounterServiceOuterClass.CounterInfo> results = airport.queryCounters(req.getSector());

        CounterServiceOuterClass.QueryCountersResponse.Builder responseBuilder
                = CounterServiceOuterClass.QueryCountersResponse
                    .newBuilder()
                    .addAllCounters(results);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCheckIns(CounterServiceOuterClass.QueryCheckInsRequest req, StreamObserver<CounterServiceOuterClass.QueryCheckInsResponse> responseObserver) {
        List<CounterServiceOuterClass.CheckInRecord> results = airport.queryCheckIns(req.getSector(), req.getAirline());

        CounterServiceOuterClass.QueryCheckInsResponse.Builder responseBuilder
                = CounterServiceOuterClass.QueryCheckInsResponse
                    .newBuilder()
                    .addAllCheckIns(results);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
