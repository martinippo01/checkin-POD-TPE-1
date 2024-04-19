package ar.edu.itba.pod.tpe1.client;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Arguments {
    SERVER_ADDRESS("serverAddress"),
    ACTION("action"),
    BOOKING("booking"),
    SECTOR("sector"),
    COUNTER("counter");

    private final String argument;

    Arguments(String argument) {
        this.argument = argument;
    }

    public String getArgument() {
        return argument;
    }

    private static final Map<String, Arguments> stringToCheckInArgumentsEnum =
            Stream.of(values())
                    .collect(Collectors.toMap(Arguments::getArgument, e -> e));

    public static Optional<Arguments> checkInArgumentsFromString(final String symbol) {
        return Optional.ofNullable(stringToCheckInArgumentsEnum.get(symbol));
    }
}
