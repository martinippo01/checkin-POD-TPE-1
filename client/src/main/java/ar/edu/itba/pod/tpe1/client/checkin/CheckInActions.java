package ar.edu.itba.pod.tpe1.client.checkin;

import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.checkin.actions.FetchCounter;
import ar.edu.itba.pod.tpe1.client.checkin.actions.PassengerCheckin;
import ar.edu.itba.pod.tpe1.client.checkin.actions.PassengerStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ar.edu.itba.pod.tpe1.client.checkin.CheckInArguments.*;

public enum CheckInActions {
    FETCH_COUNTER(
            "fetchCounter",
            new FetchCounter(List.of(BOOKING.getArgument()))
    ),
    PASSENGER_CHECK_IN(
            "passengerCheckin",
            new PassengerCheckin(List.of(BOOKING.getArgument(), SECTOR.getArgument(), COUNTER.getArgument()))
    ),
    PASSENGER_STATUS(
            "passengerStatus",
            new PassengerStatus(List.of(BOOKING.getArgument())));


    private final String actionName;
    private final Action action;

    CheckInActions(String actionName, Action action) {
        this.actionName = actionName;
        this.action = action;
    }

    public String getActionName() {
        return actionName;
    }

    public Action getAction() {
        return action;
    }

    private static final Map<String, CheckInActions> stringToCheckInActionsEnum =
            Stream.of(values())
                    .collect(Collectors.toMap(CheckInActions::getActionName, e -> e));

    public static Optional<CheckInActions> checkInActionsFromString(final String symbol) {
        return Optional.ofNullable(stringToCheckInActionsEnum.get(symbol));
    }
}
