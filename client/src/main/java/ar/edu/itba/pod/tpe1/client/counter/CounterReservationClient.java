package ar.edu.itba.pod.tpe1.client.counter;

import ar.edu.itba.pod.tpe1.client.Arguments;
import ar.edu.itba.pod.tpe1.client.Client;
import ar.edu.itba.pod.tpe1.client.Util;
import ar.edu.itba.pod.tpe1.client.checkin.CheckInActions;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static ar.edu.itba.pod.tpe1.client.checkin.CheckInActions.checkInActionsFromString;

public class CounterReservationClient extends Client {
    private static final Logger logger = LoggerFactory.getLogger(CounterReservationClient.class);

    public CounterReservationClient(String[] args) {
        super(args);
    }

    @Override
    public void executeAction() throws ServerUnavailableException {
        CheckInActions action = checkInActionsFromString(getActionArgument()).orElseThrow(() -> {
                    logger.error("Provided action '{}' doesn't exist.", Arguments.SERVER_ADDRESS.getArgument());
                    return new IllegalArgumentException(Util.EXCEPTION_MESSAGE_UNEXPECTED_ARGUMENT + Arguments.ACTION.getArgument());
                }
        );

        action.getAction().setArguments(getArguments());
        action.getAction().run(getChannel());
    }

    public static void main(String[] args) {
        try (Client client = new CounterReservationClient(args)) {
            client.executeAction();
        } catch (IllegalArgumentException e) {
            System.err.println(Util.ERROR_MESSAGE_INVALID_ARGUMENT);
            System.exit(1);
        } catch (ServerUnavailableException e) {
            System.err.println(Util.ERROR_MESSAGE_SERVER_UNAVAILABLE);
            System.exit(2);
        } catch (IOException e) {
            System.err.println(Util.ERROR_MESSAGE_IO);
            System.exit(3);
        }
    }
}
