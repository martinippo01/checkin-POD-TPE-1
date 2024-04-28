package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.tpe1.CheckInCountersRequest;
import ar.edu.itba.pod.tpe1.CheckInCountersResponse;
import ar.edu.itba.pod.tpe1.CheckinServiceGrpc;
import ar.edu.itba.pod.tpe1.client.counter.CounterReservationAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public final class CheckInCounters extends CounterReservationAction {


    private CheckinServiceGrpc.CheckinServiceBlockingStub blockingStub;

    public CheckInCounters(List<String> actionArguments){
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException{
        blockingStub = CheckinServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());
        int fromVal = Integer.parseInt(getArguments().get(COUNTER_FROM.getArgument()));
        String airlineName = getArguments().get(AIRLINE.getArgument());

        CheckInCountersRequest request = CheckInCountersRequest.newBuilder()
                .setSectorName(sectorName)
                .setAirlineName(airlineName)
                .setCounterNumber(fromVal)
                .build();
        try{
            CheckInCountersResponse response =blockingStub.performCheckIn(request);
            System.out.println(response);
        }catch(StatusRuntimeException e){
            System.err.println("RPC failed: " + e.getMessage());
        }
    }

}
