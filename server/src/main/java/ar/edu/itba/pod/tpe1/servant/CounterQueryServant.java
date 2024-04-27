package ar.edu.itba.pod.tpe1.servant;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CounterQueryServant extends CounterServiceGrpc.CounterServiceImplBase {

    private final Airport airport = Airport.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(NotificationsServant.class);

    @Override
    public void queryCounters(CounterServiceOuterClass.QueryCountersRequest req, StreamObserver<CounterServiceOuterClass.QueryCountersResponse> responseObserver) {
        try {
            logger.info("Querying counters by sector: {}", req.getSector());
            List<CounterServiceOuterClass.CounterInfo> results = airport.queryCountersBySector(req.getSector());

            CounterServiceOuterClass.QueryCountersResponse.Builder responseBuilder = CounterServiceOuterClass.QueryCountersResponse
                    .newBuilder()
                    .addAllCounters(results);

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException querying counters by sector: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalStateException e) {
            logger.error("IllegalStateException querying counters by sector: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected Exception querying counters by sector: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void queryCheckIns(CounterServiceOuterClass.QueryCheckInsRequest req, StreamObserver<CounterServiceOuterClass.QueryCheckInsResponse> responseObserver) {
        try {
            logger.info("Querying check-ins by sector: {} and airline: {}", req.getSector(), req.getAirline());
            List<CounterServiceOuterClass.CheckInRecord> results = airport.queryCheckIns(req.getSector(), req.getAirline());
            CounterServiceOuterClass.QueryCheckInsResponse.Builder responseBuilder
                    = CounterServiceOuterClass.QueryCheckInsResponse
                    .newBuilder()
                    .addAllCheckIns(results);

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e){
            logger.error("IllegalArgumentException querying check-ins by sector: {} and airline: {}", req.getSector(), req.getAirline());
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }   catch (Exception e) {
            logger.error("Unexpected Exception querying check-ins by sector: {} and airline: {}", req.getSector(), req.getAirline());
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
