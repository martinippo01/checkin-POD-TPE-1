package ar.edu.itba.pod.tpe1.client;

import airport.CounterServiceGrpc;
import airport.CounterServiceOuterClass;
import io.grpc.Channel;

public class CounterQueryClient {

        private final CounterServiceGrpc.CounterServiceBlockingStub blockingStub;

        public CounterQueryClient(Channel channel) {
            blockingStub = CounterServiceGrpc.newBlockingStub(channel);
        }

        public void queryCounters(String sector) {
            CounterServiceOuterClass.QueryCountersRequest request = CounterServiceOuterClass.QueryCountersRequest.newBuilder()
                    .setSector(sector)
                    .build();
            CounterServiceOuterClass.QueryCountersResponse response = blockingStub.queryCounters(request);
            // Format and print response
        }

        public void queryCheckIns(String sector, String airline) {
            CounterServiceOuterClass.QueryCheckInsRequest request = CounterServiceOuterClass.QueryCheckInsRequest.newBuilder()
                    .setSector(sector)
                    .setAirline(airline)
                    .build();
            CounterServiceOuterClass.QueryCheckInsResponse response = blockingStub.queryCheckIns(request);
            // Format and print response
        }
}
