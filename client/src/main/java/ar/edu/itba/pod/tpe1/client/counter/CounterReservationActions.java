package ar.edu.itba.pod.tpe1.client.counter;

import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.counter.actions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public enum CounterReservationActions {
    LIST_SECTORS(
            "listSectors",
            new ListSectors(Collections.emptyList())
    ),
    LIST_COUNTERS(
            "listCounters",
            new ListCounters(List.of(SECTOR.getArgument(), COUNTER_FROM.getArgument(), COUNTER_TO.getArgument()))
    ),
    ASSIGN_COUNTERS(
            "assignCounters",
            new AssignCounters(List.of(SECTOR.getArgument(), FLIGHTS.getArgument(), AIRLINE.getArgument(), COUNTER_COUNT.getArgument()))
    ),
    FREE_COUNTERS(
            "freeCounters",
            new FreeCounters(List.of(SECTOR.getArgument(), COUNTER_FROM.getArgument(), AIRLINE.getArgument()))
    ),
    CHECKIN_COUNTERS(
            "checkinCounters",
            new CheckInCounters(List.of(SECTOR.getArgument(), COUNTER_FROM.getArgument(), AIRLINE.getArgument()))
    ),
    LIST_PENDING_ASSIGNMENTS(
            "listPendingAssignments",
            new ListPendingAssignments(List.of(SECTOR.getArgument()))
    );


    private final String actionName;
    private final Action action;

    CounterReservationActions(String actionName, Action action) {
        this.actionName = actionName;
        this.action = action;
    }

    public String getActionName() {
        return actionName;
    }

    public Action getAction() {
        return action;
    }

    private static final Map<String, CounterReservationActions> stringToCounterReservationActionsEnum =
            Stream.of(values())
                    .collect(Collectors.toMap(CounterReservationActions::getActionName, e -> e));

    public static Optional<CounterReservationActions> counterReservationActionsFromString(final String symbol) {
        return Optional.ofNullable(stringToCounterReservationActionsEnum.get(symbol));
    }
}
