package ar.edu.itba.pod.tpe1.data.utils;

import java.util.List;
import java.util.Objects;

public class AssignedRangeCounter implements Comparable<AssignedRangeCounter> {

    private final int counterFrom;
    private final int counterTo;
    private final List<Flight> flights;
    private final Airline airline;

    public AssignedRangeCounter(final int counterFrom, final int counterTo, List<Flight> flightList, Airline airline) {
        this.counterFrom = counterFrom;
        this.counterTo = counterTo;
        this.flights = flightList; // TODO CHECK THREAD SAFETY!!!
        this.airline = airline;
    }

    public int getCounterFrom() {
        return counterFrom;
    }
    public int getCounterTo() {
        return counterTo;
    }
    public List<Flight> getFlights() {
        return flights; // TODO CHECK THREAD SAFETY!!!
    }
    public Airline getAirline() {
        return airline;
    }

    // CounterFrom is unique among assigned counters
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignedRangeCounter that = (AssignedRangeCounter) o;

        return counterFrom == that.counterFrom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterFrom);
    }

    @Override
    public int compareTo(AssignedRangeCounter assignedRangeCounter) {
        // Check which one starts first
        return Integer.compare(counterFrom, assignedRangeCounter.counterFrom);
    }
}
