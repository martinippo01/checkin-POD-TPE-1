package ar.edu.itba.pod.tpe1.client.admin;

import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.admin.actions.AddCounters;
import ar.edu.itba.pod.tpe1.client.admin.actions.AddSector;
import ar.edu.itba.pod.tpe1.client.admin.actions.Manifest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ar.edu.itba.pod.tpe1.client.Arguments.*;

public enum AirportAdminActions {
    ADD_SECTOR(
            "addSector",
            new AddSector(List.of(SECTOR.getArgument()))
    ),
    ADD_COUNTERS(
            "addCounters",
            new AddCounters(List.of(SECTOR.getArgument(), COUNTERS.getArgument()))
    ),
    MANIFEST(
            "manifest",
            new Manifest(List.of(IN_PATH.getArgument()))
    );


    private final String actionName;
    private final Action action;

    AirportAdminActions(String actionName, Action action) {
        this.actionName = actionName;
        this.action = action;
    }

    public String getActionName() {
        return actionName;
    }

    public Action getAction() {
        return action;
    }

    private static final Map<String, AirportAdminActions> stringToAirportAdminActionsEnum =
            Stream.of(values())
                    .collect(Collectors.toMap(AirportAdminActions::getActionName, e -> e));

    public static Optional<AirportAdminActions> airportAdminActionsFromString(final String symbol) {
        return Optional.ofNullable(stringToAirportAdminActionsEnum.get(symbol));
    }
}
