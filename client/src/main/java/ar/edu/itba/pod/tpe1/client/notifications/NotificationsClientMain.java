package ar.edu.itba.pod.tpe1.client.notifications;

import ar.edu.itba.pod.tpe1.client.Arguments;
import ar.edu.itba.pod.tpe1.client.Client;
import ar.edu.itba.pod.tpe1.client.Util;
import ar.edu.itba.pod.tpe1.client.exceptions.ServerUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationsClientMain extends Client {
    private static final Logger logger = LoggerFactory.getLogger(NotificationsClientMain.class);

    public NotificationsClientMain(String[] args) {
        super(args);
    }

    @Override
    public void executeAction() throws ServerUnavailableException {
        NotificationsActions action = NotificationsActions.notificationsActionsFromString(getActionArgument())
                .orElseThrow(() -> {
                    logger.error("Provided action '{}' doesn't exist.", Arguments.SERVER_ADDRESS.getArgument());
                    return new IllegalArgumentException(
                            Util.EXCEPTION_MESSAGE_UNEXPECTED_ARGUMENT + Arguments.ACTION.getArgument());
                }
        );

        action.getAction().setArguments(getArguments());
        action.getAction().run(getChannel());
    }

    public static void main(String[] args) {
//        String[] test = {
//                "-DserverAddress=localhost:50058", // -DserverAddress=10.6.0.1:50051
//                "-Daction=register",
//                "-Dairline=AmericanAirlines"
//        };
//
//        try (Client client = new NotificationsClientMain(test)) {
        try (Client client = new NotificationsClientMain(args)) {
            client.executeAction();
        } catch (IllegalArgumentException e) {
            System.err.println(Util.ERROR_MESSAGE_INVALID_ARGUMENT);
            System.exit(1);
        } catch (ServerUnavailableException e) {
            System.err.println(Util.ERROR_MESSAGE_SERVER_UNAVAILABLE);
            System.exit(2);
        } catch (Exception e) {
            System.err.println(Util.ERROR_MESSAGE_IO);
            System.exit(3);
        }
    }
}
