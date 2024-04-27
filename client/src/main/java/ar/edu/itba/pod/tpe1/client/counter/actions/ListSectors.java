package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import counter.CounterReservationServiceGrpc;
import counter.CounterReservationServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.List;

public final class ListSectors extends CounterReservationAction {
    private CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public ListSectors(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        CounterReservationServiceOuterClass.SectorRequest request = CounterReservationServiceOuterClass.SectorRequest.newBuilder().build();
        try {
            CounterReservationServiceOuterClass.SectorResponse response = blockingStub.listSectors(request);
            if (response.getSectorsList().isEmpty()) {
                System.out.println("No sectors available at the airport.");
                return;
            }
            System.out.println("Sectors   Counters");
            System.out.println("###################");
            response.getSectorsList().forEach(sector -> {
                String ranges = sector.getRangesList().stream()
                        .map(range -> String.format("(%d-%d)", range.getStart(), range.getEnd()))
                        .reduce((a, b) -> a + " " + b).orElse("-");
                System.out.printf("%s         %s\n", sector.getName(), ranges);
            });
        } catch (StatusRuntimeException e) {
            System.out.println("Failed: " + e.getStatus().asRuntimeException());
        }
    }
}
