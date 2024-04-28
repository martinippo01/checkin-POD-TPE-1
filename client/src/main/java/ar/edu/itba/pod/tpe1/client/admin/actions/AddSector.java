package ar.edu.itba.pod.tpe1.client.admin.actions;

import ar.edu.itba.pod.tpe1.client.admin.AirportAdminAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import ar.edu.itba.pod.tpe1.protos.AirportService.AirportAdminServiceGrpc;
import ar.edu.itba.pod.tpe1.protos.AirportService.SectorRequest;
import ar.edu.itba.pod.tpe1.protos.AirportService.SectorResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;

import static ar.edu.itba.pod.tpe1.client.Arguments.SECTOR;

public class AddSector extends AirportAdminAction {
    private AirportAdminServiceGrpc.AirportAdminServiceBlockingStub blockingStub;

    public AddSector(List<String> actionArguments) {
        super(actionArguments);
    }

    @Override
    public void run(ManagedChannel channel) throws ServerUnavailableException {
        blockingStub = AirportAdminServiceGrpc.newBlockingStub(channel);

        String sectorName = getArguments().get(SECTOR.getArgument());

        try {
            SectorRequest request = SectorRequest.newBuilder().setSectorName(sectorName).build();
            SectorResponse response = blockingStub.addSector(request);

            System.out.println("Sector " + response.getSectorName() + " added successfully");
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                System.err.println("Failed to add sector: ");
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new ServerUnavailableException();
            } else {
                System.err.println("Failed to add sector: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
}
