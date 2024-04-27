package ar.edu.itba.pod.tpe1.client.admin.actions;

import airport.AirportAdminServiceGrpc;
import airport.AirportService;
import ar.edu.itba.pod.tpe1.client.admin.AirportAdminAction;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;

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

        AirportService.SectorRequest request = AirportService.SectorRequest.newBuilder().setSectorName(sectorName).build();
        AirportService.SectorResponse response = blockingStub.addSector(request);
        // TODO: Check Response in Servant
        // if (response.getStatus() == AirportService.ResponseStatus.SUCCESS) {
        //     System.out.println("Sector " + response.getSectorName() + " added successfully");
        // } else {
        //     System.out.println("Failed to add sector: " + response.getSectorName());
        // }
    }
}
