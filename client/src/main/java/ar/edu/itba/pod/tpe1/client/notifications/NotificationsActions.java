package ar.edu.itba.pod.tpe1.client.notifications;

import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.notifications.actions.RegisterNotifications;
import ar.edu.itba.pod.tpe1.client.notifications.actions.RemoveNotifications;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public enum NotificationsActions {
    REGISTER(
            "register",
            new RegisterNotifications(List.of(AIRLINE.getArgument()))
    ),
    UNREGISTER(
            "unregister",
            new RemoveNotifications(List.of(AIRLINE.getArgument()))
    );

    private final String actionName;
    private final Action action;

    NotificationsActions(String actionName, Action action) {
        this.actionName = actionName;
        this.action = action;
    }

    public String getActionName() {
        return actionName;
    }

    public Action getAction() {
        return action;
    }

    private static final Map<String, NotificationsActions> stringToNotificationsActionsEnum =
            Stream.of(values())
                    .collect(Collectors.toMap(NotificationsActions::getActionName, e -> e));

    public static Optional<NotificationsActions> notificationsActionsFromString(final String symbol) {
        return Optional.ofNullable(stringToNotificationsActionsEnum.get(symbol));
    }
}
