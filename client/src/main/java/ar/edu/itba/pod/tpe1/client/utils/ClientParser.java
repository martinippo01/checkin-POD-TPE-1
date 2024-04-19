package ar.edu.itba.pod.tpe1.client.utils;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ClientParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientParser.class);
    private static final String SERVER_ADDRESS_PROP     = "serverAddress";
    private static final String ACTION_PROP             = "action"; // TODO: list of actions??
    private static final String SECTOR_PROP             = "sector";
    private static final String COUNTERS_PROP           = "counters";
    private static final String IN_PATH_PROP            = "inPath";
    private static final String COUNTER_FROM_PROP       = "counterFrom";
    private static final String COUNTER_TO_PROP         = "counterTo";
    private static final String FLIGHTS_PROP            = "flights";
    private static final String AIRLINE_PROP            = "airline";
    private static final String COUNTER_COUNT_PROP      = "counterCount";
    private static final String BOOKING_PROP            = "booking";
    private static final String COUNTER_PROP            = "counter";
    private static final String OUT_PATH_PROP           = "outPath";

    public static ManagedChannel buildChannel(String serverAddress) {
        return ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
    }

    public static Map<String, String> parse(String[] args) {
        Map<String, String> properties = new HashMap<>();
        for (String arg : args) {
            String[] split = arg.substring(2).split("=");
            if (split.length == 2) {
                properties.put(split[0], split[1]);
            }
        }
        return properties;
    }

    public static String getPropertiesValue(Map<String, String> properties, String key) {
        return properties.get(key);
    }

    public static void validateNullArgument(String property, String propertyError) {
        if (property == null) {
            LOGGER.error(propertyError);
            System.exit(1);
        }
    }
}
