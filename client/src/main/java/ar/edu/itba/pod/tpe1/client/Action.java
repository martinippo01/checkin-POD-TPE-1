package ar.edu.itba.pod.tpe1.client;

import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ServiceUnavailableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Action {
    private static final Logger logger = LoggerFactory.getLogger(Action.class);

    private final List<String> actionArguments;
    private final HashMap<String, String> arguments = new HashMap<>(1);


    public Action(final List<String> actionArguments) {
        Objects.requireNonNull(actionArguments, "Received NULL 'actionArguments'");
        this.actionArguments = actionArguments;
    }

    public abstract void run(final ManagedChannel channel) throws ServerUnavailableException;

    public List<String> getActionArguments() {
        return actionArguments;
    }

    public HashMap<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(final Map<String, String> arguments) {
        Objects.requireNonNull(arguments, "Received NULL 'arguments'");

        for (String actionArgument : actionArguments) {
            if (!arguments.containsKey(actionArgument)) {
                logger.error("Expected {} argument, but found nothing.", actionArgument);
                throw new IllegalArgumentException(Util.EXCEPTION_MESSAGE_INVALID_ARGUMENT + actionArgument);
            }

            this.arguments.put(actionArgument, arguments.get(actionArgument));
        }
    }
}
