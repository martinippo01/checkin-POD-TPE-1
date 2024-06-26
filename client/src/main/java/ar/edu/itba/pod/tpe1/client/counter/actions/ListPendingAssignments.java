package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.CounterReservationServiceGrpc;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.PendingAssignmentsRequest;
import ar.edu.itba.pod.tpe1.protos.CounterReservation.PendingAssignmentsResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.SECTOR;

public final class ListPendingAssignments extends CounterReservationAction {
    private CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public ListPendingAssignments(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = CounterReservationServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());

        PendingAssignmentsRequest request = PendingAssignmentsRequest.newBuilder()
                .setSectorName(sectorName)
                .build();
        try {
            PendingAssignmentsResponse response = blockingStub.listPendingAssignments(request);
            if (response.getAssignmentsList().isEmpty()) {
                System.out.println("No pending assignments.");
                return;
            }
            System.out.println("Counters  Airline          Flights");
            System.out.println("##################################################");
            response.getAssignmentsList().forEach(assignment -> {
                String flights = String.join("|", assignment.getFlightsList());
                System.out.printf("%d         %s        %s\n",
                        assignment.getCounterCount(), assignment.getAirlineName(), flights);
            });
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                System.err.println("Error: The specified sector '" + sectorName + "' does not exist.");
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
