package ar.edu.itba.pod.tpe1.client.checkin;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.naming.ServiceUnavailableException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ar.edu.itba.pod.tpe1.client.checkin.CheckInActions.checkInActionsFromString;

public final class CheckInClient {
    private static Map<String, String> getArgumentsAsMap(String[] args) {
        List<String> validArgs = Arrays.stream(args)
                .filter(arg -> arg.startsWith("-D"))
                .filter(arg -> arg.contains("="))
                .map(arg -> arg.substring(2))
                .toList();

        HashMap<String, String> argumentsAsMap = new HashMap<>(validArgs.size());
        for (String arg : validArgs) {
            int equalSignIndex = arg.indexOf("=");

            argumentsAsMap.put(arg.substring(0, equalSignIndex), arg.substring(equalSignIndex + 1));
        }

        return argumentsAsMap;
    }

    private static void executeAction(CheckInActions action, Map<String, String> arguments) throws InterruptedException {
        if (!arguments.containsKey(CheckInArguments.SERVER_ADDRESS.getArgument())) {
            throw new IllegalArgumentException("Argument '" + CheckInArguments.SERVER_ADDRESS.getArgument() + "' is required.");
        }

        int semicolonIndex = arguments.get(CheckInArguments.SERVER_ADDRESS.getArgument()).indexOf(":");
        String serverAddress = arguments.get(CheckInArguments.SERVER_ADDRESS.getArgument()).substring(0, semicolonIndex);
        int serverPort = Integer.parseInt(arguments.get(CheckInArguments.SERVER_ADDRESS.getArgument()).substring(semicolonIndex + 1));

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        try {
            action.getAction().setArgumentsValues(arguments);
            action.getAction().run(channel);
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Map<String, String> argsMap = getArgumentsAsMap(args);

        if (!argsMap.containsKey(CheckInArguments.ACTION.getArgument())) {
            throw new IllegalArgumentException("Expected 'action' argument.");
        }

        CheckInActions action = checkInActionsFromString(argsMap.get(CheckInArguments.ACTION.getArgument())).orElseThrow(() ->
                new IllegalArgumentException("Provided action '" + CheckInArguments.ACTION.getArgument() + "' does not exists.")
        );

        executeAction(action, argsMap);
    }
}
