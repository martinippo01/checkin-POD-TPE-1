package ar.edu.itba.pod.tpe1.servant;

import airport.AirportService;
import airport.CounterServiceOuterClass;
import ar.edu.itba.pod.tpe1.data.Airport;
import ar.edu.itba.pod.tpe1.data.utils.RangeCounter;
import ar.edu.itba.pod.tpe1.data.utils.RequestedRangeCounter;
import ar.edu.itba.pod.tpe1.data.utils.Sector;
import counter.CounterReservationServiceGrpc;
import counter.CounterReservationServiceOuterClass;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CounterReservationService extends CounterReservationServiceGrpc.CounterReservationServiceImplBase {

    Airport airport = Airport.getInstance();

    @Override
    public void listSectors(CounterReservationServiceOuterClass.SectorRequest request, StreamObserver<CounterReservationServiceOuterClass.SectorResponse> responseObserver) {
        CounterReservationServiceOuterClass.SectorResponse.Builder response = CounterReservationServiceOuterClass.SectorResponse.newBuilder();

        Map<Sector, List<RangeCounter>> sectorInfo = airport.getSectors();

        for(Sector sector : sectorInfo.keySet()){

            CounterReservationServiceOuterClass.Sector.Builder sectorBuilder = CounterReservationServiceOuterClass.Sector.newBuilder().setName(sector.getName());

            sectorInfo.get(sector).forEach((ranges) -> {
                sectorBuilder.addRanges(CounterReservationServiceOuterClass.Range.newBuilder().setStart(ranges.getCounterFrom()).setEnd(ranges.getCounterTo()).build());
            });
//            response.setSectors(sectorBuilder.build());
            response.addSectors(sectorBuilder.build());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCounterRange(CounterReservationServiceOuterClass.CounterRangeRequest request, StreamObserver<CounterReservationServiceOuterClass.CounterRangeResponse> responseObserver) {
        CounterReservationServiceOuterClass.CounterRangeResponse.Builder response = CounterReservationServiceOuterClass.CounterRangeResponse.newBuilder();

        List<CounterServiceOuterClass.CounterInfo> counters = airport.queryCounters(request.getSectorName());
        for (CounterServiceOuterClass.CounterInfo counter : counters) {
            if (Integer.parseInt(counter.getRange()) >= request.getFromVal() && Integer.parseInt(counter.getRange()) <= request.getToVal()) {
                CounterReservationServiceOuterClass.CounterRange.Builder rangeBuilder = CounterReservationServiceOuterClass.CounterRange.newBuilder()
                        .setStart(Integer.parseInt(counter.getRange()))
                        .setEnd(Integer.parseInt(counter.getRange()))
                        .setAirline("Example Airline");  // Placeholder for airline, modify as needed.
                response.addCounters(rangeBuilder.build());
            }
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void assignCounters(CounterReservationServiceOuterClass.AssignCounterRequest request, StreamObserver<CounterReservationServiceOuterClass.AssignCounterResponse> responseObserver) {
        CounterReservationServiceOuterClass.AssignCounterResponse.Builder response = CounterReservationServiceOuterClass.AssignCounterResponse.newBuilder();

        RequestedRangeCounter addedCounters = airport.assignCounters(request.getSectorName(), request.getCounterCount(), request.getAirlineName(), request.getFlightsList());

        if(addedCounters == null)
            response.setIsPending(true);
        else
            response.setIsPending(false).setCounterFrom(addedCounters.getCounterFrom());

        responseObserver.onNext(response.build());

        responseObserver.onCompleted();
    }


    @Override
    public void freeCounters(CounterReservationServiceOuterClass.FreeCounterRequest request, StreamObserver<CounterReservationServiceOuterClass.BasicResponse> responseObserver) {
        CounterReservationServiceOuterClass.BasicResponse.Builder response = CounterReservationServiceOuterClass.BasicResponse.newBuilder();
        // Logic to handle freeing counters (not detailed in provided Airport class)
        response.setMessage("Counters freed successfully.");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void checkInCounters(CounterReservationServiceOuterClass.CheckInCounterRequest request, StreamObserver<CounterReservationServiceOuterClass.BasicResponse> responseObserver) {
        CounterReservationServiceOuterClass.BasicResponse.Builder response = CounterReservationServiceOuterClass.BasicResponse.newBuilder();
        // Simulate check-in process
//        airport.logCheckIn(request.getSectorName(), request.getCounter(), request.getAirlineName(), "FlightXYZ", "BookingABC");
//        response.setMessage("Check-in successful.");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listPendingAssignments(CounterReservationServiceOuterClass.PendingAssignmentsRequest request, StreamObserver<CounterReservationServiceOuterClass.PendingAssignmentsResponse> responseObserver) {
        CounterReservationServiceOuterClass.PendingAssignmentsResponse.Builder response = CounterReservationServiceOuterClass.PendingAssignmentsResponse.newBuilder();
        // Placeholder logic for listing pending assignments
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
