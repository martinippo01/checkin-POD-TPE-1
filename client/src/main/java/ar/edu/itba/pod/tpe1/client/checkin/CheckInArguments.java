package ar.edu.itba.pod.tpe1.client.checkin;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CheckInArguments {
    SERVER_ADDRESS("serverAddress"),
    ACTION("action"),
    BOOKING("booking"),
    SECTOR("sector"),
    COUNTER("counter");

    private final String argument;

    CheckInArguments(String argument) {
        this.argument = argument;
    }

    public String getArgument() {
        return argument;
    }

    private static final Map<String, CheckInArguments> stringToCheckInArgumentsEnum =
            Stream.of(values())
                    .collect(Collectors.toMap(CheckInArguments::getArgument, e -> e));

    public static Optional<CheckInArguments> checkInArgumentsFromString(final String symbol) {
        return Optional.ofNullable(stringToCheckInArgumentsEnum.get(symbol));
    }
}
