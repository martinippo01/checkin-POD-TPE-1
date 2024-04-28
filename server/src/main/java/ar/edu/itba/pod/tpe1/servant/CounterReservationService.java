package ar.edu.itba.pod.tpe1.servant;

import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.utils.Sector;
import ar.edu.itba.pod.tpe1.data.utils.*;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CounterReservationService extends CounterReservationServiceGrpc.CounterReservationServiceImplBase {

    Airport airport = Airport.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(CounterReservationService.class);

    @Override
    public void listSectors(SectorRequest request, StreamObserver<SectorResponse> responseObserver) {
        try {
            logger.info("Requesting sectors from airport");
            SectorResponse.Builder response = SectorResponse.newBuilder();

            Map<Sector, List<RangeCounter>> sectorInfo = airport.getSectors();

            for (Sector sector : sectorInfo.keySet()) {

                ar.edu.itba.pod.tpe1.protos.CounterReservation.Sector.Builder sectorBuilder = ar.edu.itba.pod.tpe1.protos.CounterReservation.Sector.newBuilder().setName(sector.getName());

                sectorInfo.get(sector).forEach((ranges) -> {
                    sectorBuilder.addRanges(ar.edu.itba.pod.tpe1.protos.CounterReservation.Range.newBuilder().setStart(ranges.getCounterFrom()).setEnd(ranges.getCounterTo()).build());
                });
                response.addSectors(sectorBuilder.build());
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException listing sectors: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalStateException e) {
            logger.error("IllegalArgumentException listing sectors: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("IllegalArgumentException Error listing sectors: {}", e.getMessage());
            responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void queryCounterRange(CounterRangeRequest request, StreamObserver<CounterRangeResponse> responseObserver) {
        try {
            logger.info("Requesting counter range from airport with sector: {}, from: {}, to: {}", request.getSectorName(), request.getFromVal(), request.getToVal());
            CounterRangeResponse.Builder response = CounterRangeResponse.newBuilder();
            List<RequestedRangeCounter> counters = airport.listCounters(request.getSectorName(), request.getFromVal(), request.getToVal());

            for (RequestedRangeCounter counter : counters) {
                CounterRange.Builder rangeBuilder = CounterRange.newBuilder()
                        .setStart(counter.getCounterFrom())
                        .setEnd(counter.getCounterTo())
                        .setAirline(counter.getAirline().getName())
                        .addAllFlights(counter.getFlights().stream().map(Flight::getFlightCode).collect(Collectors.toList()));

                response.addCounters(rangeBuilder.build());
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException querying counter range: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalStateException e) {
            logger.error("IllegalStateException querying counter range: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Undefined Exception querying counter range: {}", e.getMessage());
            responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void assignCounters(AssignCounterRequest request, StreamObserver<AssignCounterResponse> responseObserver) {
        try {
            logger.info("Assigning counters from airport with sector: {}, count: {}, airline: {}", request.getSectorName(), request.getCounterCount(), request.getAirlineName());
            AssignCounterResponse.Builder response = AssignCounterResponse.newBuilder();
            RequestedRangeCounter addedCounters = airport.assignCounters(request.getSectorName(), request.getCounterCount(), request.getAirlineName(), request.getFlightsList());

            if (addedCounters == null)
                response.setIsPending(true);
            else
                response.setIsPending(false).setCounterFrom(addedCounters.getCounterFrom());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException assigning counters: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException());
        } catch (Exception e) {
            logger.error("Undefined Error assigning counters: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asException());
        }

    }

    @Override
    public void freeCounters(FreeCounterRequest request, StreamObserver<FreeCounterResponse> responseObserver) {
        try {
            logger.info("Freeing counters from airport with sector: {}, from: {}, airline: {}", request.getSectorName(), request.getFromVal(), request.getAirlineName());
            FreeCounterResult result = airport.freeCounters(request.getSectorName(), request.getFromVal(), request.getAirlineName());
            FreeCounterResponse response = FreeCounterResponse.newBuilder()
                    .setSectorName(result.getSectorName())
                    .setRangeStart(result.getRangeStart())
                    .setRangeEnd(result.getRangeEnd())
                    .setAirlineName(result.getAirlineName())
                    .addAllFlightNumbers(result.getFlights())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException freeing counters: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException freeing counters: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalCallerException e) {
            logger.error("IllegalCallerException freeing counters: {}", e.getMessage());
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalStateException e) {
            logger.error("IllegalStateException freeing counters: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Undefined Exception freeing counters: {}", e.getMessage());
            responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void checkInCounters(CheckInCounterRequest request, StreamObserver<BasicResponse> responseObserver) {
        BasicResponse.Builder response = BasicResponse.newBuilder();
        // Simulate check-in process
//        airport.logCheckIn(request.getSectorName(), request.getCounter(), request.getAirlineName(), "FlightXYZ", "BookingABC");
//        response.setMessage("Check-in successful.");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listPendingAssignments(PendingAssignmentsRequest request, StreamObserver<PendingAssignmentsResponse> responseObserver) {
        try {
            logger.info("Listing pending assignments from airport with sector: {}", request.getSectorName());
            PendingAssignmentsResponse.Builder response = PendingAssignmentsResponse.newBuilder();
            List<RequestedRangeCounter> requestedRangeCounters = airport.listPendingRequestedCounters(request.getSectorName());

            for (RequestedRangeCounter requestedRangeCounter : requestedRangeCounters) {
                response.addAssignments(
                        PendingAssignment.newBuilder()
                                .setCounterCount(requestedRangeCounter.getRequestedRange())
                                .setAirlineName(requestedRangeCounter.getAirline().getName())
                                .addAllFlights(requestedRangeCounter.getFlights().stream().map(Flight::getFlightCode).collect(Collectors.toList()))
                                .build()
                );
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException listing pending assignments: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException());
        }
    }
}
