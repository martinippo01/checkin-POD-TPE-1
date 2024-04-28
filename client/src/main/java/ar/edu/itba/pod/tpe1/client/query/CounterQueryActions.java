package ar.edu.itba.pod.tpe1.client.query;

import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.query.actions.CheckIns;
import ar.edu.itba.pod.tpe1.client.query.actions.QueryCounters;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public enum CounterQueryActions {
    QUERY_COUNTERS(
            "queryCounters",
            new QueryCounters(List.of(OUT_PATH.getArgument()), List.of(SECTOR.getArgument()))
    ),
    CHECKINS(
            "checkins",
            new CheckIns(List.of(OUT_PATH.getArgument()), List.of(SECTOR.getArgument(), AIRLINE.getArgument()))
    );

    private final String actionName;
    private final Action action;

    CounterQueryActions(String actionName, Action action) {
        this.actionName = actionName;
        this.action = action;
    }

    public String getActionName() {
        return actionName;
    }

    public Action getAction() {
        return action;
    }

    private static final Map<String, CounterQueryActions> stringToCounterQueryActionsEnum =
            Stream.of(values())
                    .collect(Collectors.toMap(CounterQueryActions::getActionName, e -> e));

    public static Optional<CounterQueryActions> counterQueryActionsFromString(final String symbol) {
        return Optional.ofNullable(stringToCounterQueryActionsEnum.get(symbol));
    }
}
