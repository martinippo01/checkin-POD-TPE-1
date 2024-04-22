package ar.edu.itba.pod.tpe1.data.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Airline {

    private final String name;

    // Counters assigned to each flight. Many Flights may share the same RequestedRangeCounter
    private ConcurrentHashMap<Flight, TreeSet<RequestedRangeCounter>> requestedCounters;

    public Airline(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SortedSet<RequestedRangeCounter> getRequestedCounters(Flight flight) {
        return requestedCounters.get(flight);
    }

    public void addRequestedCounters(List<Flight> flights, RequestedRangeCounter requestedCounter) {
        flights.forEach(flight -> addRequestedCounters(flight, requestedCounter));
    }

    public void addRequestedCounters(Flight flight, RequestedRangeCounter requestedCounter) {
        TreeSet<RequestedRangeCounter> counters = requestedCounters.get(flight);
        counters.add(requestedCounter);

        requestedCounters.put(flight, counters);
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airline airline = (Airline) o;
        return Objects.equals(name, airline.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
