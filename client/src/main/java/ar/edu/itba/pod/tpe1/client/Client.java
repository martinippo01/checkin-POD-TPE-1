package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class Client implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final ManagedChannel channel;
    private final Map<String, String> arguments;

    public Client(String[] args) {
        Map<String, String> argsMap = getArgumentsAsMap(args);

        if (!argsMap.containsKey(Arguments.ACTION.getArgument())) {
            logger.error("Action expects an argument for {} but found nothing.", Arguments.ACTION.getArgument());
            throw new IllegalArgumentException(Util.EXCEPTION_MESSAGE_INVALID_ARGUMENT + Arguments.ACTION.getArgument());
        }

        if (argsMap.get(Arguments.ACTION.getArgument()).isEmpty()) {
            logger.warn("Argument {} found to be empty. This may be an error.", Arguments.ACTION.getArgument());
        }

        this.arguments = argsMap;
        this.channel = buildChannel(argsMap);

        logger.debug("Client successfully initialized.");
    }

    private Map<String, String> getArgumentsAsMap(String[] args) {
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

        logger.debug("Found arguments: {}", argumentsAsMap);

        return argumentsAsMap;
    }

    private static ManagedChannel buildChannel(Map<String, String> arguments) {
        if (!arguments.containsKey(Arguments.SERVER_ADDRESS.getArgument())) {
            logger.error("Expected {} argument, but found nothing.", Arguments.SERVER_ADDRESS.getArgument());
            throw new IllegalArgumentException(Util.EXCEPTION_MESSAGE_INVALID_ARGUMENT + Arguments.SERVER_ADDRESS.getArgument());
        }

        int semicolonIndex = arguments.get(Arguments.SERVER_ADDRESS.getArgument()).indexOf(":");
        String serverAddress = arguments.get(Arguments.SERVER_ADDRESS.getArgument()).substring(0, semicolonIndex);
        int serverPort = Integer.parseInt(arguments.get(Arguments.SERVER_ADDRESS.getArgument()).substring(semicolonIndex + 1));

        logger.debug("Server to connect: address={} | port={}", serverAddress, serverPort);

        return ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();
    }

    public String getActionArgument() {
        return getArguments().get(Arguments.ACTION.getArgument());
    }

    public Map<String, String> getArguments() {
        return this.arguments;
    }

    public abstract void executeAction();

    public ManagedChannel getChannel() {
        return this.channel;
    }

    @Override
    public void close() throws IOException {
        try {
            int timeout = 10;
            logger.debug("About to shutdown connection. This may take up to {} seconds", timeout);
            channel.shutdown().awaitTermination(timeout, TimeUnit.SECONDS);
            logger.info("Channel shutdown successfully.");
        } catch (InterruptedException e) {
            logger.error("Channel shutdown interrupted.");
            throw new IOException(e);
        }
    }
}
